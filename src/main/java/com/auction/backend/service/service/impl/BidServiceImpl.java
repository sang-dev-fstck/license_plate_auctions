package com.auction.backend.service.service.impl;

import com.auction.backend.common.MoneyUtils;
import com.auction.backend.dto.PlaceBidRequest;
import com.auction.backend.dto.PlaceBidResponse;
import com.auction.backend.entity.*;
import com.auction.backend.enums.AuctionSessionStatus;
import com.auction.backend.enums.BidStatus;
import com.auction.backend.enums.ParticipationStatus;
import com.auction.backend.exception.AppException;
import com.auction.backend.repository.AuctionParticipationRepository;
import com.auction.backend.repository.AuctionSessionRepository;
import com.auction.backend.repository.BidRepository;
import com.auction.backend.repository.WalletRepository;
import com.auction.backend.security.CurrentAccountProvider;
import com.auction.backend.service.BidService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class BidServiceImpl implements BidService {
    private final BidRepository bidRepository;
    private final WalletRepository walletRepository;
    private final CurrentAccountProvider currentAccountProvider;
    private final AuctionSessionRepository auctionSessionRepository;
    private final AuctionParticipationRepository auctionParticipationRepository;

    @Override
    public PlaceBidResponse placeBid(PlaceBidRequest request) {
        Account user = currentAccountProvider.getCurrentAccount();

        AuctionSession session = auctionSessionRepository.findById(request.getAuctionSessionId())
                .orElseThrow(() -> new AppException("Phiên đấu giá không hợp lệ hoặc không tồn tại"));

        validateSessionCanBid(session);

        AuctionParticipation participation = auctionParticipationRepository.findByAuctionSessionIdAndAccountId(session.getId(), user.getId())
                .orElseThrow(() -> new AppException("Người dùng chưa đặt cọc cho session này"));
        validateParticipation(participation);

        validateBidAmount(request.getAmount(), session);

        Wallet wallet = walletRepository.findByAccountId(user.getId())
                .orElseThrow(() -> new AppException("Tài khoản không tồn tại ví, vui lòng kiểm tra lại !"));

        boolean isCurrentLeader = isCurrentLeader(session, user);
        BigDecimal oldCurrentPrice = session.getCurrentPrice();

        if (participation.getStatus() == ParticipationStatus.RESERVED) {

            wallet.unfreeze(participation.getDepositAmount());
            wallet.freeze(request.getAmount());

            Wallet previousLeaderWallet = getPreviousLeaderWallet(session, user);

            try {
                walletRepository.save(wallet);

                if (previousLeaderWallet != null) {
                    previousLeaderWallet.unfreeze(oldCurrentPrice);
                    walletRepository.save(previousLeaderWallet);
                }

                Bid bid = saveBidAndUpdateSession(request, user, session);
                participation.setStatus(ParticipationStatus.CONSUMED);
                participation.setLastBidAmount(request.getAmount());
                auctionParticipationRepository.save(participation);

                return PlaceBidResponse.builder()
                        .bidId(bid.getId())
                        .licensePlateNumber(session.getLicensePlateNumber())
                        .bidAmount(request.getAmount())
                        .message("Đặt giá thành công với giá " + MoneyUtils.format(request.getAmount()) + "VNĐ")
                        .currentPrice(request.getAmount())
                        .auctionSessionId(session.getId())
                        .build();
            } catch (Exception e) {
                try {

                    wallet.unfreeze(request.getAmount());
                    wallet.freeze(participation.getDepositAmount());
                    walletRepository.save(wallet);

                    if (previousLeaderWallet != null) {
                        previousLeaderWallet.freeze(oldCurrentPrice);
                        walletRepository.save(previousLeaderWallet);
                    }

                } catch (Exception rollbackEx) {
                    log.error("Rollback bid flow failed", rollbackEx);
                }
                throw new AppException(e.getMessage());
            }
        }

        if (participation.getStatus() == ParticipationStatus.CONSUMED && isCurrentLeader) {
            if (participation.getLastBidAmount() != null) {
                BigDecimal delta = request.getAmount().subtract(participation.getLastBidAmount());
                wallet.freeze(delta);
                try {
                    walletRepository.save(wallet);
                    return getPlaceBidResponse(request, user, session, participation);
                } catch (Exception e) {
                    try {
                        wallet.unfreeze(delta);
                        walletRepository.save(wallet);
                    } catch (Exception rollbackEx) {
                        log.error(String.valueOf(rollbackEx));
                    }
                    throw new AppException(e.getMessage());
                }
            } else {
                throw new AppException("Hệ thống đang gặp lỗi");
            }
        }

        Wallet previousLeaderWallet = getPreviousLeaderWallet(session, user);

        if (previousLeaderWallet == null) {
            throw new AppException("Không tìm được ví của previous");
        }

        previousLeaderWallet.unfreeze(oldCurrentPrice);

        wallet.freeze(request.getAmount());

        try {
            walletRepository.save(previousLeaderWallet);
            walletRepository.save(wallet);

            return getPlaceBidResponse(request, user, session, participation);
        } catch (Exception e) {
            try {
                wallet.unfreeze(request.getAmount());
                walletRepository.save(wallet);

                previousLeaderWallet.freeze(oldCurrentPrice);
                walletRepository.save(previousLeaderWallet);

            } catch (Exception rollbackEx) {
                log.error("Rollback bid flow failed", rollbackEx);
            }
            throw new AppException(e.getMessage());
        }
    }

    private PlaceBidResponse getPlaceBidResponse(PlaceBidRequest request, Account user, AuctionSession session, AuctionParticipation participation) {
        Bid bid = saveBidAndUpdateSession(request, user, session);

        participation.setLastBidAmount(request.getAmount());
        auctionParticipationRepository.save(participation);

        return PlaceBidResponse.builder()
                .bidId(bid.getId())
                .licensePlateNumber(session.getLicensePlateNumber())
                .bidAmount(request.getAmount())
                .message("Đặt giá thành công với giá " + MoneyUtils.format(request.getAmount()) + "VNĐ")
                .currentPrice(request.getAmount())
                .auctionSessionId(session.getId())
                .build();
    }

    private Bid saveBidAndUpdateSession(PlaceBidRequest request, Account user, AuctionSession session) {
        Bid bid = Bid.builder()
                .auctionSessionId(session.getId())
                .amount(request.getAmount())
                .bidderAccountId(user.getId())
                .status(BidStatus.PLACED)
                .build();
        session.setCurrentPrice(request.getAmount());
        session.setCurrentLeaderAccountId(user.getId());
        auctionSessionRepository.save(session);
        return bidRepository.save(bid);
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

    private Wallet getPreviousLeaderWallet(AuctionSession session, Account user) {
        if (session.getCurrentLeaderAccountId() == null) {
            return null;
        }
        if (session.getCurrentLeaderAccountId().equals(user.getId())) {
            return null;
        }
        return walletRepository.findByAccountId(session.getCurrentLeaderAccountId()).orElse(null);
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

    private boolean isCurrentLeader(AuctionSession session, Account user) {
        return session.getCurrentLeaderAccountId() != null && session.getCurrentLeaderAccountId().equals(user.getId());
    }
}
