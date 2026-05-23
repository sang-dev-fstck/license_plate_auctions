package com.auction.backend.service.service.impl;

import com.auction.backend.common.MoneyUtils;
import com.auction.backend.dto.AuctionSessionRealtimeEvent;
import com.auction.backend.dto.PlaceBidRequest;
import com.auction.backend.dto.PlaceBidResponse;
import com.auction.backend.entity.*;
import com.auction.backend.enums.AuctionSessionStatus;
import com.auction.backend.enums.BidStatus;
import com.auction.backend.enums.EventType;
import com.auction.backend.enums.ParticipationStatus;
import com.auction.backend.exception.AppException;
import com.auction.backend.repository.*;
import com.auction.backend.security.CurrentAccountProvider;
import com.auction.backend.service.AuctionSessionRealtimeService;
import com.auction.backend.service.BidService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class BidServiceImpl implements BidService {
    private final BidRepository bidRepository;
    private final WalletRepository walletRepository;
    private final WalletAtomicRepository walletAtomicRepository;
    private final CurrentAccountProvider currentAccountProvider;
    private final AuctionSessionRepository auctionSessionRepository;
    private final AuctionParticipationRepository auctionParticipationRepository;
    private final AuctionSessionAtomicRepository auctionSessionAtomicRepository;
    private final AuctionSessionRealtimeService auctionSessionRealtimeService;

    @Override
    public PlaceBidResponse placeBid(PlaceBidRequest request) {
        int maxAttempts = 3;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return placeBidOnce(request);
            } catch (OptimisticLockingFailureException e) {
                if (attempt == maxAttempts) {
                    throw new AppException("Có người vừa đặt giá trước bạn, vui lòng thử lại");
                }
                log.warn("Optimistic locking conflict when placing bid. attempt={}", attempt);
            }
        }
        throw new AppException("Không thể đặt giá, vui lòng thử lại");
    }

    private PlaceBidResponse placeBidOnce(PlaceBidRequest request) {
        Account user = currentAccountProvider.getCurrentAccount();

        AuctionSession session = auctionSessionRepository.findById(request.getAuctionSessionId())
                .orElseThrow(() -> new AppException("Phiên đấu giá không hợp lệ hoặc không tồn tại"));

        validateSessionCanBid(session);

        AuctionParticipation participation = auctionParticipationRepository
                .findByAuctionSessionIdAndAccountId(session.getId(), user.getId())
                .orElseThrow(() -> new AppException("Người dùng chưa đặt cọc cho session này"));

        validateParticipation(participation);
        validateBidAmount(request.getAmount(), session);

        boolean isCurrentLeader = isCurrentLeader(session, user);

        if (participation.getStatus() == ParticipationStatus.RESERVED) {
            return placeFirstBidFromReserved(request, user, session, participation, user.getId());
        }

        if (participation.getStatus() == ParticipationStatus.CONSUMED && isCurrentLeader) {
            return rebidAsCurrentLeader(request, user, session, participation, user.getId());
        }

        return outbidCurrentLeader(request, user, session, participation, user.getId());
    }

    private PlaceBidResponse placeFirstBidFromReserved(
            PlaceBidRequest request,
            Account user,
            AuctionSession session,
            AuctionParticipation participation,
            String accountId
    ) {
        ParticipationSnapshot participationSnapshot = snapshot(participation);

        String previousLeaderAccountId = session.getCurrentLeaderAccountId();

        BigDecimal previousLeaderAmountToRelease = previousLeaderAccountId != null
                ? getPreviousLeaderAmountToRelease(session)
                : null;
        BigDecimal additionalAmount = request.getAmount().subtract(participation.getDepositAmount());

        if (additionalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException("Giá đặt phải lớn hơn tiền đặt cọc");
        }

        boolean walletAdjusted = false;

        try {
            if (additionalAmount.compareTo(BigDecimal.ZERO) > 0) {
                walletAtomicRepository.freezeAvailable(accountId, additionalAmount);
                walletAdjusted = true;
            }

            participation.setStatus(ParticipationStatus.CONSUMED);
            participation.setLastBidAmount(request.getAmount());
            auctionParticipationRepository.save(participation);

            advanceSessionForBid(request, user, session);
        } catch (OptimisticLockingFailureException | AppException e) {
            if (walletAdjusted) {
                safeReleaseFrozen(accountId, additionalAmount, "rollback first bid from reserved");
            }
            restoreParticipation(participation, participationSnapshot);
            throw e;

        } catch (Exception e) {
            if (walletAdjusted) {
                safeReleaseFrozen(accountId, additionalAmount, "rollback first bid from reserved");
            }
            restoreParticipation(participation, participationSnapshot);
            log.error("Failed to place first bid from RESERVED", e);
            throw new AppException("Không thể đặt giá, vui lòng thử lại");
        }
        reportLeaderConsistency(session);
        Bid bid = saveBidHistoryAfterAccepted(request, user, session);
        releasePreviousLeaderAfterSuccessfulBid(
                previousLeaderAccountId,
                previousLeaderAmountToRelease,
                session.getId()
        );

        publishBidAcceptedEvent(session);
        return getBidResponse(request, session, bid);
    }

    private PlaceBidResponse rebidAsCurrentLeader(
            PlaceBidRequest request,
            Account user,
            AuctionSession session,
            AuctionParticipation participation,
            String accountId
    ) {
        if (participation.getLastBidAmount() == null) {
            throw new AppException("Dữ liệu đặt giá của người dùng không hợp lệ");
        }

        BigDecimal delta = request.getAmount().subtract(participation.getLastBidAmount());

        if (delta.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException("Giá đặt mới phải lớn hơn giá đã đặt trước đó");
        }

        ParticipationSnapshot participationSnapshot = snapshot(participation);

        boolean walletAdjusted = false;
        try {
            walletAtomicRepository.freezeAvailable(accountId, delta);
            walletAdjusted = true;

            participation.setLastBidAmount(request.getAmount());
            auctionParticipationRepository.save(participation);

            advanceSessionForBid(request, user, session);
        } catch (OptimisticLockingFailureException | AppException e) {
            if (walletAdjusted) {
                safeReleaseFrozen(accountId, delta, "rollback current leader rebid");
            }
            restoreParticipation(participation, participationSnapshot);
            throw e;

        } catch (Exception e) {
            if (walletAdjusted) {
                safeReleaseFrozen(accountId, delta, "rollback current leader rebid");
            }
            restoreParticipation(participation, participationSnapshot);
            log.error("Failed to rebid as current leader", e);
            throw new AppException("Không thể đặt giá, vui lòng thử lại");
        }
        reportLeaderConsistency(session);
        Bid bid = saveBidHistoryAfterAccepted(request, user, session);
        publishBidAcceptedEvent(session);
        return getBidResponse(request, session, bid);
    }

    private PlaceBidResponse outbidCurrentLeader(
            PlaceBidRequest request,
            Account user,
            AuctionSession session,
            AuctionParticipation participation,
            String accountId
    ) {
        String previousLeaderAccountId = session.getCurrentLeaderAccountId();

        if (previousLeaderAccountId == null) {
            throw new AppException("Không tìm được leader hiện tại");
        }

        BigDecimal amountToRelease = getPreviousLeaderAmountToRelease(session);

        ParticipationSnapshot participationSnapshot = snapshot(participation);
        boolean walletAdjusted = false;
        try {

            walletAtomicRepository.freezeAvailable(accountId, request.getAmount());
            walletAdjusted = true;
            participation.setLastBidAmount(request.getAmount());
            auctionParticipationRepository.save(participation);
            advanceSessionForBid(request, user, session);
        } catch (OptimisticLockingFailureException | AppException e) {
            if (walletAdjusted) {
                safeReleaseFrozen(accountId, request.getAmount(), "rollback outbid current leader");
            }
            restoreParticipation(participation, participationSnapshot);
            throw e;

        } catch (Exception e) {
            if (walletAdjusted) {
                safeReleaseFrozen(accountId, request.getAmount(), "rollback outbid current leader");
            }
            restoreParticipation(participation, participationSnapshot);
            log.error("Failed to outbid current leader", e);
            throw new AppException("Không thể đặt giá, vui lòng thử lại");
        }

        reportLeaderConsistency(session);
        releasePreviousLeaderAfterSuccessfulBid(
                previousLeaderAccountId,
                amountToRelease,
                session.getId()
        );
        Bid bid = saveBidHistoryAfterAccepted(request, user, session);
        publishBidAcceptedEvent(session);
        return getBidResponse(request, session, bid);
    }

    private PlaceBidResponse getBidResponse(
            PlaceBidRequest request,
            AuctionSession session,
            Bid bid
    ) {
        return PlaceBidResponse.builder()
                .bidId(bid != null ? bid.getId() : null)
                .licensePlateNumber(session.getLicensePlateNumber())
                .bidAmount(request.getAmount())
                .endTime(session.getEndTime())
                .message("Đặt giá thành công với giá " + MoneyUtils.format(request.getAmount()) + " VNĐ")
                .currentPrice(request.getAmount())
                .auctionSessionId(session.getId())
                .build();
    }

    private Bid saveBidHistoryAfterAccepted(
            PlaceBidRequest request,
            Account user,
            AuctionSession session
    ) {
        try {
            return bidRepository.save(buildBid(request, user, session));
        } catch (Exception e) {
            log.error(
                    "Bid accepted but failed to save bid history. sessionId={}, bidderId={}, amount={}",
                    session.getId(),
                    user.getId(),
                    request.getAmount(),
                    e
            );
            return null;
        }
    }

    private Bid buildBid(PlaceBidRequest request, Account user, AuctionSession session) {
        return Bid.builder()
                .auctionSessionId(session.getId())
                .amount(request.getAmount())
                .bidderAccountId(user.getId())
                .bidderFullNameSnapshot(user.getFullName())
                .status(BidStatus.PLACED)
                .build();
    }

    private void advanceSessionForBid(PlaceBidRequest request, Account user, AuctionSession session) {
        LocalDateTime newEndTime = calculateNewEndTime(session, request.getAmount());

        auctionSessionAtomicRepository.advanceBid(
                session,
                request.getAmount(),
                user.getId(),
                user.getFullName(),
                newEndTime
        );

        session.setCurrentPrice(request.getAmount());
        session.setCurrentLeaderAccountId(user.getId());
        session.setCurrentLeaderNameSnapshot(user.getFullName());
        session.setEndTime(newEndTime);
    }

    private void releasePreviousLeaderAfterSuccessfulBid(
            String previousLeaderAccountId,
            BigDecimal amountToRelease,
            String sessionId
    ) {
        if (previousLeaderAccountId == null || amountToRelease == null) {
            return;
        }

        try {
            walletAtomicRepository.releaseFrozen(previousLeaderAccountId, amountToRelease);
        } catch (Exception e) {
            log.error(
                    "Failed to release previous leader wallet after successful bid. sessionId={}, leaderAccountId={}, amountToRelease={}",
                    sessionId,
                    previousLeaderAccountId,
                    amountToRelease,
                    e
            );
        }
    }

    private BigDecimal getPreviousLeaderAmountToRelease(AuctionSession session) {
        if (session.getCurrentLeaderAccountId() == null) {
            return null;
        }

        AuctionParticipation previousLeaderParticipation =
                auctionParticipationRepository.findByAuctionSessionIdAndAccountId(
                        session.getId(),
                        session.getCurrentLeaderAccountId()
                ).orElseThrow(() -> new AppException("Không tìm thấy participation của leader hiện tại"));

        BigDecimal amountToRelease = previousLeaderParticipation.getLastBidAmount();

        if (amountToRelease == null || amountToRelease.compareTo(session.getCurrentPrice()) != 0) {
            throw new AppException("Dữ liệu leader hiện tại không đồng bộ, vui lòng thử lại");
        }

        return amountToRelease;
    }

    private void validateParticipation(AuctionParticipation participation) {
        ParticipationStatus status = participation.getStatus();

        if (status != ParticipationStatus.CONSUMED
                && status != ParticipationStatus.RESERVED) {
            throw new AppException("Người dùng chưa đặt cọc cho phiên đấu giá này");
        }
    }

    private void validateBidAmount(BigDecimal amount, AuctionSession session) {
        BigDecimal stepPrice = session.getBidStepAmountSnapshot();
        BigDecimal currentPrice = session.getCurrentPrice();
        BigDecimal validPBidAmount = currentPrice.add(stepPrice);

        if (amount.compareTo(validPBidAmount) < 0) {
            throw new AppException(String.format(
                    "Giá đặt phải ≥ %sVNĐ (giá hiện tại %sVNĐ + bước giá %sVNĐ)",
                    MoneyUtils.format(validPBidAmount),
                    MoneyUtils.format(currentPrice),
                    MoneyUtils.format(stepPrice)
            ));
        }
    }

    private void validateSessionCanBid(AuctionSession session) {
        if (session.getStatus() != AuctionSessionStatus.ACTIVE) {
            throw new AppException("Phiên đấu giá hiện không cho phép đặt giá");
        }
        LocalDateTime now = LocalDateTime.now();

        if (session.getStartTime() == null || session.getEndTime() == null) {
            throw new AppException("Phiên đấu giá có dữ liệu thời gian không hợp lệ");
        }

        if (now.isBefore(session.getStartTime()) || now.isAfter(session.getEndTime())) {
            throw new AppException("Phiên đấu giá hiện không nằm trong thời gian nhận đặt giá");
        }
    }

    private boolean isCurrentLeader(AuctionSession session, Account user) {
        return session.getCurrentLeaderAccountId() != null && session.getCurrentLeaderAccountId().equals(user.getId());
    }

    private LocalDateTime calculateNewEndTime(AuctionSession session, BigDecimal newBidAmount) {
        LocalDateTime now = LocalDateTime.now();
        BigDecimal oldCurrentPrice = session.getCurrentPrice();
        if (newBidAmount.compareTo(oldCurrentPrice.multiply(BigDecimal.TEN)) >= 0) {
            return now.plusMinutes(1);
        }
        Duration remainingTime =
                Duration.between(now, session.getEndTime());

        // Nếu còn dưới 5 phút thì mới extend
        if (remainingTime.toMinutes() < 5) {
            return session.getEndTime().plusMinutes(5);
        }

        // Còn >= 5 phút -> giữ nguyên
        return session.getEndTime();
    }

    private ParticipationSnapshot snapshot(AuctionParticipation participation) {
        return new ParticipationSnapshot(
                participation.getStatus(),
                participation.getLastBidAmount()
        );
    }

    private void restoreParticipation(AuctionParticipation participation, ParticipationSnapshot snapshot) {
        if (participation == null || snapshot == null) {
            return;
        }

        try {
            participation.restoreState(snapshot.status(), snapshot.lastBidAmount());
            auctionParticipationRepository.save(participation);
        } catch (Exception rollbackEx) {
            log.error("Failed to restore participation state. participationId={}", participation.getId(), rollbackEx);
        }
    }

    private void safeReleaseFrozen(String accountId, BigDecimal amount, String reason) {
        if (accountId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        try {
            walletAtomicRepository.releaseFrozen(accountId, amount);
        } catch (Exception rollbackEx) {
            log.error("Failed to rollback frozen wallet. accountId={}, amount={}, reason={}",
                    accountId, amount, reason, rollbackEx);
        }
    }

    private void verifyLeaderConsistency(AuctionSession session) {
        if (session.getCurrentLeaderAccountId() == null) {
            return;
        }

        AuctionParticipation leaderParticipation =
                auctionParticipationRepository.findByAuctionSessionIdAndAccountId(
                        session.getId(),
                        session.getCurrentLeaderAccountId()
                ).orElseThrow(() -> new AppException("Không tìm thấy participation của leader hiện tại"));

        Wallet leaderWallet = walletRepository.findByAccountId(session.getCurrentLeaderAccountId())
                .orElseThrow(() -> new AppException("Không tìm thấy ví của leader hiện tại"));

        BigDecimal currentPrice = session.getCurrentPrice();
        BigDecimal lastBidAmount = leaderParticipation.getLastBidAmount();
        BigDecimal frozenBalance = leaderWallet.getFrozenBalance();

        if (lastBidAmount == null || lastBidAmount.compareTo(currentPrice) != 0) {
            log.error(
                    "Bid consistency violation: lastBidAmount mismatch. sessionId={}, leaderId={}, currentPrice={}, lastBidAmount={}",
                    session.getId(),
                    session.getCurrentLeaderAccountId(),
                    currentPrice,
                    lastBidAmount
            );
            throw new AppException("Dữ liệu phiên đấu giá không đồng bộ, vui lòng thử lại");
        }

        if (frozenBalance.compareTo(currentPrice) < 0) {
            log.error(
                    "Bid consistency violation: frozen balance insufficient. sessionId={}, leaderId={}, currentPrice={}, frozenBalance={}",
                    session.getId(),
                    session.getCurrentLeaderAccountId(),
                    currentPrice,
                    frozenBalance
            );
            throw new AppException("Dữ liệu ví của leader không đồng bộ, vui lòng thử lại");
        }

        log.info(
                "Bid consistency verified. sessionId={}, leaderId={}, currentPrice={}, frozenBalance={}",
                session.getId(),
                session.getCurrentLeaderAccountId(),
                currentPrice,
                frozenBalance
        );
    }

    private void publishBidAcceptedEvent(AuctionSession session) {
        try {
            AuctionSessionRealtimeEvent event = AuctionSessionRealtimeEvent.builder()
                    .type(EventType.BID_ACCEPTED)
                    .auctionSessionId(session.getId())
                    .currentPrice(session.getCurrentPrice())
                    .currentLeaderNameSnapshot(session.getCurrentLeaderNameSnapshot())
                    .endTime(session.getEndTime())
                    .status(session.getStatus())
                    .occurredAt(LocalDateTime.now())
                    .build();
            auctionSessionRealtimeService.publish(session.getId(), event);
        } catch (Exception e) {
            log.warn("Failed to publish bid accepted event. sessionId={}", session.getId());
        }
    }

    private void reportLeaderConsistency(AuctionSession session) {

        try {
            verifyLeaderConsistency(session);
        } catch (Exception e) {
            log.error(
                    "Post-bid consistency check failed. sessionId={}, leaderId={}, currentPrice={}",
                    session.getId(),
                    session.getCurrentLeaderAccountId(),
                    session.getCurrentPrice(),
                    e
            );
        }
    }

    private record ParticipationSnapshot(
            ParticipationStatus status,
            BigDecimal lastBidAmount
    ) {
    }
}
