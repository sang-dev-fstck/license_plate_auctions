package com.auction.backend.service.service.impl;

import com.auction.backend.dto.JoinAuctionSessionRequest;
import com.auction.backend.dto.JoinAuctionSessionResponse;
import com.auction.backend.entity.Account;
import com.auction.backend.entity.AuctionParticipation;
import com.auction.backend.entity.AuctionSession;
import com.auction.backend.entity.Wallet;
import com.auction.backend.enums.AuctionSessionStatus;
import com.auction.backend.enums.ParticipationStatus;
import com.auction.backend.exception.AppException;
import com.auction.backend.repository.AuctionParticipationRepository;
import com.auction.backend.repository.AuctionSessionRepository;
import com.auction.backend.repository.WalletRepository;
import com.auction.backend.security.CurrentAccountProvider;
import com.auction.backend.service.AuctionParticipationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionParticipationServiceImpl implements AuctionParticipationService {
    private final WalletRepository walletRepository;
    private final AuctionSessionRepository auctionSessionRepository;
    private final AuctionParticipationRepository auctionParticipationRepository;
    private final CurrentAccountProvider currentAccountProvider;

    @Override
    public JoinAuctionSessionResponse joinAuctionSession(JoinAuctionSessionRequest request) {
        Account user = currentAccountProvider.getCurrentAccount();

        AuctionSession session = auctionSessionRepository.findById(request.getAuctionSessionId())
                .orElseThrow(() -> new AppException("Auction Session Not Found"));
        validateSessionCanReserve(session);

        boolean alreadyReserved = auctionParticipationRepository.findByAuctionSessionIdAndAccountIdAndStatus(
                session.getId(),
                user.getId(),
                ParticipationStatus.RESERVED
        ).isPresent();

        if (alreadyReserved) {
            throw new AppException("Bạn đã đặt cọc tham gia phiên đấu giá này rồi");
        }

        Wallet wallet = walletRepository.findByAccountId(user.getId())
                .orElseThrow(() -> new AppException("Không tìm thấy ví của người dùng hiện tại"));

        BigDecimal depositAmount = session.getStartingPrice();

        if (depositAmount == null) {
            throw new AppException("Phiên đấu giá chưa có giá khởi điểm hợp lệ");
        }
        wallet.freeze(depositAmount);
        Wallet savedWallet = walletRepository.save(wallet);

        try {
            AuctionParticipation participation = AuctionParticipation.builder()
                    .auctionSessionId(session.getId())
                    .accountId(user.getId())
                    .depositAmount(depositAmount)
                    .lastBidAmount(BigDecimal.ZERO)
                    .status(ParticipationStatus.RESERVED)
                    .build();
            AuctionParticipation savedParticipation = auctionParticipationRepository.save(participation);
            log.info("Participation reserved: participationId={}, sessionId={}, accountId={}",
                    savedParticipation.getId(), session.getId(), user.getId());

            return JoinAuctionSessionResponse.builder()
                    .participationId(savedParticipation.getId())
                    .auctionSessionId(session.getId())
                    .licensePlateNumber(session.getLicensePlateNumber())
                    .depositAmount(savedParticipation.getDepositAmount())
                    .message("Đặt cọc tham gia phiên đấu giá thành công")
                    .build();

        } catch (Exception e) {
            try {
                savedWallet.unfreeze(depositAmount);
                walletRepository.save(savedWallet);
            } catch (Exception rollbackEx) {
                log.error("Rollback reserve deposit failed for walletId={}", savedWallet.getId(), rollbackEx);
            }
            throw new AppException("Không thể hoàn tất đặt cọc tham gia phiên đấu giá");
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
