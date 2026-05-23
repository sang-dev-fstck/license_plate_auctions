package com.auction.backend.service.service.impl;

import com.auction.backend.dto.JoinAuctionSessionRequest;
import com.auction.backend.dto.JoinAuctionSessionResponse;
import com.auction.backend.entity.Account;
import com.auction.backend.entity.AuctionParticipation;
import com.auction.backend.entity.AuctionSession;
import com.auction.backend.enums.AuctionSessionStatus;
import com.auction.backend.enums.ParticipationStatus;
import com.auction.backend.exception.AppException;
import com.auction.backend.repository.AuctionParticipationRepository;
import com.auction.backend.repository.AuctionSessionRepository;
import com.auction.backend.repository.WalletAtomicRepository;
import com.auction.backend.security.CurrentAccountProvider;
import com.auction.backend.service.AuctionParticipationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionParticipationServiceImpl implements AuctionParticipationService {
    private final AuctionSessionRepository auctionSessionRepository;
    private final WalletAtomicRepository walletAtomicRepository;
    private final AuctionParticipationRepository auctionParticipationRepository;
    private final CurrentAccountProvider currentAccountProvider;

    @Override
    public JoinAuctionSessionResponse joinAuctionSession(JoinAuctionSessionRequest request) {
        Account user = currentAccountProvider.getCurrentAccount();

        AuctionSession session = auctionSessionRepository.findById(request.getAuctionSessionId())
                .orElseThrow(() -> AppException.notFound("Auction Session Not Found"));

        validateSessionCanReserve(session);
        BigDecimal depositAmount = session.getStartingPrice();

        if (depositAmount == null) {
            throw new AppException("Phiên đấu giá chưa có giá khởi điểm hợp lệ");
        }
        boolean walletAdjusted = false;
        try {
            walletAtomicRepository.freezeAvailable(user.getId(), depositAmount);
            walletAdjusted = true;

            AuctionParticipation participation = AuctionParticipation.builder()
                    .auctionSessionId(session.getId())
                    .accountId(user.getId())
                    .depositAmount(depositAmount)
                    .status(ParticipationStatus.RESERVED)
                    .build();
            AuctionParticipation savedParticipation = auctionParticipationRepository.save(participation);
            return getParticipationResponse(session, savedParticipation);
        } catch (DuplicateKeyException e) {
            if (walletAdjusted) {
                safeReleaseFrozen(user.getId(), depositAmount, "rollback duplicate reserve participation");
            }
            throw AppException.conflict(
                    "auctionSessionId",
                    "Bạn đã tham gia phiên đấu giá này"
            );
        } catch (AppException e) {
            if (walletAdjusted) {
                safeReleaseFrozen(user.getId(), depositAmount, "rollback reserve participation business error");
            }
            throw e;
        } catch (Exception e) {
            if (walletAdjusted) {
                safeReleaseFrozen(user.getId(), depositAmount, "rollback reserve participation unexpected error");
            }
            log.error("Failed to reserve auction session. sessionId={}, accountId={}",
                    session.getId(),
                    user.getId(),
                    e
            );
            throw new AppException("Không thể tham gia phiên đấu giá, vui lòng thử lại");
        }
    }

    private JoinAuctionSessionResponse getParticipationResponse(
            AuctionSession session,
            AuctionParticipation participation
    ) {
        return JoinAuctionSessionResponse.builder()
                .participationId(participation.getId())
                .auctionSessionId(session.getId())
                .licensePlateNumber(session.getLicensePlateNumber())
                .depositAmount(participation.getDepositAmount())
                .message("Đặt cọc tham gia phiên đấu giá thành công")
                .build();
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

    private void validateSessionCanReserve(AuctionSession session) {
        if (session.getStatus() != AuctionSessionStatus.SCHEDULED) {
            throw new AppException("Phiên đấu giá hiện không cho phép đặt cọc tham gia");
        }
        LocalDateTime now = LocalDateTime.now();

        if (session.getStartTime() == null) {
            throw new AppException("Phiên đấu giá có dữ liệu thời gian không hợp lệ");
        }

        if (!now.isBefore(session.getStartTime())) {
            throw new AppException("Phiên đấu giá đã bắt đầu hoặc đã qua thời điểm cho phép đặt cọc trước");
        }

    }

}
