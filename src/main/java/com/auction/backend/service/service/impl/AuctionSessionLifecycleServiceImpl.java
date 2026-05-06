package com.auction.backend.service.service.impl;

import com.auction.backend.dto.AuctionSessionResponse;
import com.auction.backend.dto.SessionLifecycleRequest;
import com.auction.backend.entity.AuctionSession;
import com.auction.backend.entity.LicensePlate;
import com.auction.backend.enums.AuctionSessionStatus;
import com.auction.backend.enums.LicensePlateStatus;
import com.auction.backend.enums.VehicleType;
import com.auction.backend.exception.AppException;
import com.auction.backend.mapper.AuctionSessionMapper;
import com.auction.backend.repository.AuctionSessionRepository;
import com.auction.backend.repository.LicensePlateRepository;
import com.auction.backend.service.AuctionSessionLifecycleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionSessionLifecycleServiceImpl implements AuctionSessionLifecycleService {
    private final AuctionSessionRepository auctionSessionRepository;
    private final AuctionSessionMapper auctionSessionMapper;
    private final LicensePlateRepository licensePlateRepository;

    @Override
    public AuctionSessionResponse activateSession(String sessionId) {
        LocalDateTime now = LocalDateTime.now();
        AuctionSession validSession = getSessionBySessionId(sessionId);
        if (!now.isBefore(validSession.getStartTime())
                && now.isBefore(validSession.getEndTime())
                && validSession.getStatus().equals(AuctionSessionStatus.SCHEDULED)) {
            validSession.setStatus(AuctionSessionStatus.ACTIVE);
            AuctionSession savedSession = auctionSessionRepository.save(validSession);
            return auctionSessionMapper.toResponse(savedSession);
        }
        throw new AppException("Điều kiện kích hoạt phiên đấu giá không hợp lệ");
    }

    @Override
    public AuctionSessionResponse pauseOrResumeSession(String sessionId, SessionLifecycleRequest request) {
        LocalDateTime now = LocalDateTime.now();
        AuctionSession validSession = getSessionBySessionId(sessionId);
        if (!now.isBefore(validSession.getStartTime())
                && !now.isAfter(validSession.getEndTime())
                && validSession.getStatus().equals(AuctionSessionStatus.ACTIVE)
        ) {
            long remainingTimeBeforePause = calculateRemainingSecondsBeforePause(validSession);
            validSession.setStatus(AuctionSessionStatus.PAUSED);
            validSession.setRemainingSecondsWhenPaused(remainingTimeBeforePause);
            validSession.setPauseReason(request.getReason());
            AuctionSession savedSession = auctionSessionRepository.save(validSession);
            return auctionSessionMapper.toResponse(savedSession);
        }

        if (!now.isBefore(validSession.getStartTime())
                && validSession.getStatus().equals(AuctionSessionStatus.PAUSED)
        ) {
            validSession.setStatus(AuctionSessionStatus.ACTIVE);
            validSession.setEndTime(now.plusSeconds(validSession.getRemainingSecondsWhenPaused()));
            validSession.setRemainingSecondsWhenPaused(null);
            AuctionSession savedSession = auctionSessionRepository.save(validSession);
            return auctionSessionMapper.toResponse(savedSession);
        }
        throw new AppException("Điều kiện ngừng phiên đấu giá không hợp lệ");
    }

    @Override
    public AuctionSessionResponse failSession(String sessionId, SessionLifecycleRequest request) {
        AuctionSession validSession = getSessionBySessionId(sessionId);
        LicensePlate validPlates = getLicensePlateByLicensePlateId(validSession.getLicensePlateId());

        AuctionSessionStatus initialStatus = validSession.getStatus();
        LicensePlateStatus initialPlateStatus = validPlates.getStatus();
        BigDecimal initialPlatePrice = validPlates.getNextAuctionStartPrice();
        boolean isCar = validPlates.getVehicleType().equals(VehicleType.CAR);

        if (validSession.getStatus().equals(AuctionSessionStatus.ACTIVE)
                || validSession.getStatus().equals(AuctionSessionStatus.PAUSED)
                || validSession.getStatus().equals(AuctionSessionStatus.SCHEDULED)
        ) {

            validSession.setStatus(AuctionSessionStatus.FAILED);
            validSession.setFailureReason(request.getReason());
            validPlates.setStatus(LicensePlateStatus.AVAILABLE);
            if (isCar) {
                validPlates.setNextAuctionStartPrice(BigDecimal.valueOf(400000000));
            }

            try {
                AuctionSession savedSession = auctionSessionRepository.save(validSession);
                licensePlateRepository.save(validPlates);
                return auctionSessionMapper.toResponse(savedSession);
            } catch (Exception e) {
                try {
                    validSession.setStatus(initialStatus);
                    validSession.setFailureReason(null);
                    validPlates.setStatus(initialPlateStatus);
                    validPlates.setNextAuctionStartPrice(initialPlatePrice);

                    auctionSessionRepository.save(validSession);
                    licensePlateRepository.save(validPlates);
                } catch (Exception ex) {
                    log.error("Failed to rollback");
                }
            }
        }
        throw new AppException("Không thể cập nhật trạng thái fail của phiên đấu giá");
    }

    private AuctionSession getSessionBySessionId(String sessionId) {
        AuctionSession session = auctionSessionRepository.findById(sessionId)
                .orElseThrow(() -> new AppException("Phiên đấu giá không tồn tại"));

        if (session.getStartTime() == null || session.getEndTime() == null) {
            throw new AppException("Phiên đấu giá có thời gian không hợp lệ");
        }
        return session;
    }

    private LicensePlate getLicensePlateByLicensePlateId(String licensePlateId) {
        return licensePlateRepository.findById(licensePlateId)
                .orElseThrow(() -> new AppException("Biển số xe không tồn tại"));
    }

    private long calculateRemainingSecondsBeforePause(AuctionSession session) {
        LocalDateTime now = LocalDateTime.now();
        if (session.getEndTime() == null || session.getStartTime() == null) {
            throw new AppException("Phiên đấu giá có thời gian không hợp lệ");
        }
        long remainingTime = Duration.between(now, session.getEndTime()).toSeconds();
        if (remainingTime <= 0) {
            throw new AppException("Phiên đấu giá đã kết thúc");
        }
        return remainingTime;
    }
}
