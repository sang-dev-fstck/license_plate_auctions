package com.auction.backend.service.service.impl;

import com.auction.backend.dto.AuctionSessionRealtimeEvent;
import com.auction.backend.dto.AuctionSessionResponse;
import com.auction.backend.dto.SessionLifecycleRequest;
import com.auction.backend.entity.AuctionParticipation;
import com.auction.backend.entity.AuctionSession;
import com.auction.backend.entity.LicensePlate;
import com.auction.backend.enums.*;
import com.auction.backend.exception.AppException;
import com.auction.backend.mapper.AuctionSessionMapper;
import com.auction.backend.repository.*;
import com.auction.backend.service.AuctionSessionCacheService;
import com.auction.backend.service.AuctionSessionLifecycleService;
import com.auction.backend.service.AuctionSessionRealtimeService;
import com.auction.backend.service.AuctionSessionStatusHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionSessionLifecycleServiceImpl implements AuctionSessionLifecycleService {
    private final AuctionSessionRepository auctionSessionRepository;
    private final AuctionSessionMapper auctionSessionMapper;
    private final LicensePlateRepository licensePlateRepository;
    private final WalletAtomicRepository walletAtomicRepository;
    private final AuctionParticipationRepository auctionParticipationRepository;
    private final AuctionSettlementBulkRepository auctionSettlementBulkRepository;
    private final AuctionSessionStatusHistoryService statusHistoryService;
    private final AuctionSessionRealtimeService auctionSessionRealtimeService;
    private final AuctionSessionLifecycleAtomicRepository auctionSessionLifecycleAtomicRepository;
    private final AuctionSessionCacheService auctionSessionCacheService;

    @Override
    public AuctionSessionResponse activateSession(String sessionId) {
        LocalDateTime now = LocalDateTime.now();
        AuctionSession validSession = getSessionBySessionId(sessionId);
        LicensePlate validPlate = getLicensePlateByLicensePlateId(validSession.getLicensePlateId());
        AuctionSessionStatus oldStatus = validSession.getStatus();
        if (!now.isBefore(validSession.getStartTime())
                && now.isBefore(validSession.getEndTime())
                && validSession.getStatus().equals(AuctionSessionStatus.SCHEDULED)) {

            if (validSession.getReservedCount() == null || validSession.getReservedCount() == 0) {
                validSession.setStatus(AuctionSessionStatus.ENDED);
                validPlate.setStatus(LicensePlateStatus.AVAILABLE);
                try {
                    licensePlateRepository.save(validPlate);
                    AuctionSession savedSession = auctionSessionRepository.save(validSession);
                    recordAndPublishStatusChange(savedSession, oldStatus, "Auto end session by system for there is no reservation", StatusChangedByType.SYSTEM, EventType.SESSION_ENDED);
                    return auctionSessionMapper.toResponse(savedSession);
                } catch (Exception e) {
                    log.error("End session fail with id={}", sessionId, e);
                    throw new AppException("Không thể tự động kết thúc phiên đấu giá không có người tham gia");
                }
            }

            validSession.setStatus(AuctionSessionStatus.ACTIVE);
            AuctionSession savedSession = auctionSessionRepository.save(validSession);
            evictSessionDetailCache(sessionId);
            recordAndPublishStatusChange(savedSession, oldStatus, "Auto activate session by system", StatusChangedByType.SYSTEM, EventType.SESSION_STARTED);
            return auctionSessionMapper.toResponse(savedSession);
        }
        throw new AppException("Điều kiện kích hoạt phiên đấu giá không hợp lệ");
    }

    @Override
    public AuctionSessionResponse pauseOrResumeSession(String sessionId, SessionLifecycleRequest request) {
        try {
            return pauseOrResumeSessionOnce(sessionId, request);
        } catch (OptimisticLockingFailureException e) {
            throw new AppException("Có người vừa sửa trạng thái phiên đấu giá trước bạn, vui lòng thử lại");
        }
    }

    private AuctionSessionResponse pauseOrResumeSessionOnce(String sessionId, SessionLifecycleRequest request) {
        LocalDateTime now = LocalDateTime.now();
        AuctionSession validSession = getSessionBySessionId(sessionId);
        AuctionSessionStatus oldStatus = validSession.getStatus();
        if (!now.isBefore(validSession.getStartTime())
                && !now.isAfter(validSession.getEndTime())
                && validSession.getStatus().equals(AuctionSessionStatus.ACTIVE)
        ) {
            long remainingTimeBeforePause = calculateRemainingSecondsBeforePause(validSession);
            validSession.setStatus(AuctionSessionStatus.PAUSED);
            validSession.setRemainingSecondsWhenPaused(remainingTimeBeforePause);
            validSession.setPauseReason(request.getReason());
            AuctionSession savedSession = auctionSessionRepository.save(validSession);
            evictSessionDetailCache(sessionId);
            recordAndPublishStatusChange(savedSession, oldStatus, request.getReason(), StatusChangedByType.ADMIN, EventType.SESSION_PAUSED);
            return auctionSessionMapper.toResponse(savedSession);
        }

        if (!now.isBefore(validSession.getStartTime())
                && validSession.getStatus().equals(AuctionSessionStatus.PAUSED)
        ) {
            validSession.setStatus(AuctionSessionStatus.ACTIVE);
            validSession.setEndTime(now.plusSeconds(validSession.getRemainingSecondsWhenPaused()));
            validSession.setRemainingSecondsWhenPaused(null);
            AuctionSession savedSession = auctionSessionRepository.save(validSession);
            evictSessionDetailCache(sessionId);
            recordAndPublishStatusChange(savedSession, oldStatus, null, StatusChangedByType.ADMIN, EventType.SESSION_RESUMED);
            return auctionSessionMapper.toResponse(savedSession);
        }
        throw new AppException("Không thể ngừng/tiếp tục phiên đấu giá");
    }

    @Override
    public AuctionSessionResponse failSession(String sessionId, SessionLifecycleRequest request) {
        AuctionSession validSession = getSessionBySessionId(sessionId);
        LicensePlate validPlate = getLicensePlateByLicensePlateId(validSession.getLicensePlateId());
        AuctionSessionStatus oldStatus = validSession.getStatus();
        List<AuctionParticipation> participations = auctionParticipationRepository.findByAuctionSessionId(sessionId);
        if (validSession.getStatus().equals(AuctionSessionStatus.ACTIVE)
                || validSession.getStatus().equals(AuctionSessionStatus.PAUSED)
                || validSession.getStatus().equals(AuctionSessionStatus.SCHEDULED)
        ) {
            try {
                settleFailedSessionRefunds(validSession, validPlate, participations, request);
                licensePlateRepository.save(validPlate);
                AuctionSession savedSession = auctionSessionRepository.save(validSession);
                evictSessionDetailCache(sessionId);
                recordAndPublishStatusChange(savedSession, oldStatus, request.getReason(), StatusChangedByType.ADMIN, EventType.SESSION_FAILED);
                return auctionSessionMapper.toResponse(savedSession);
            } catch (AppException e) {
                throw e;
            } catch (Exception e) {
                log.error("Failed to settle failSession for sessionId={}", validSession.getId(), e);
                throw new AppException("Không thể đánh dấu phiên đấu giá thất bại");
            }
        }
        throw new AppException("Không thể đánh dấu phiên đấu giá thất bại");
    }

    @Override
    public AuctionSessionResponse endSession(String sessionId, SessionLifecycleRequest request) {
        LocalDateTime now = LocalDateTime.now();
        boolean claimed = auctionSessionLifecycleAtomicRepository.claimEnding(sessionId, now);
        if (!claimed) {
            throw AppException.conflict(
                    "auctionSessionId",
                    "Phiên đấu giá đang được xử lý hoặc đã được kết thúc"
            );
        }
        return completeClaimedEndingSession(sessionId, request.getReason(), StatusChangedByType.ADMIN, now);
    }

    @Override
    public void autoEndSession(String sessionId) {
        LocalDateTime now = LocalDateTime.now();
        boolean claimed = auctionSessionLifecycleAtomicRepository.claimEnding(sessionId, now);
        if (!claimed) {
            log.info("Skip auto end because session was already claimed or no longer eligible. sessionId={}", sessionId);
            return;
        }
        completeClaimedEndingSession(sessionId, "Auto end session by system", StatusChangedByType.SYSTEM, now);
    }

    private AuctionSessionResponse completeClaimedEndingSession(String sessionId, String reason, StatusChangedByType changedByType, LocalDateTime now) {
        AuctionSession validSession = getSessionBySessionId(sessionId);
        LicensePlate validPlate = getLicensePlateByLicensePlateId(validSession.getLicensePlateId());
        String currentLeaderId = validSession.getCurrentLeaderAccountId();
        List<AuctionParticipation> participations = getAllParticipations(validSession.getId());

        if (!validSession.getStatus().equals(AuctionSessionStatus.ENDING)
                || now.isBefore(validSession.getEndTime())) {
            throw new AppException("Không thể kết thúc phiên đấu giá");
        }

        try {
            if (currentLeaderId == null) {
                endSessionWithoutWinner(validSession, validPlate, participations);
            } else {
                endSessionWithWinner(currentLeaderId, validSession, validPlate, participations);
            }
            licensePlateRepository.save(validPlate);
            validSession.setEndingClaimedAt(null);
            AuctionSession savedSession = auctionSessionRepository.save(validSession);
            evictSessionDetailCache(sessionId);
            recordAndPublishStatusChange(savedSession, AuctionSessionStatus.ACTIVE, reason, changedByType, EventType.SESSION_ENDED);
            return auctionSessionMapper.toResponse(savedSession);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to settle endSession for sessionId={}", validSession.getId(), e);
            throw new AppException("Không thể kết thúc phiên đấu giá");
        }
    }

    private void endSessionWithWinner(
            String winnerId,
            AuctionSession session,
            LicensePlate plate,
            List<AuctionParticipation> participations
    ) {
        if (winnerId == null || winnerId.isEmpty()) {
            throw new AppException("Dữ liệu phiên đấu giá không đồng bộ: Không có leader");
        }
        session.setStatus(AuctionSessionStatus.ENDED);
        session.setWinnerAccountId(winnerId);
        plate.setStatus(LicensePlateStatus.SOLD);
        List<AuctionParticipation> reserveParticipations = participations.stream()
                .filter(p -> !winnerId.equals(p.getAccountId()))
                .filter(p -> p.getStatus().equals(ParticipationStatus.RESERVED))
                .toList();

        walletAtomicRepository.debitFrozen(winnerId, session.getCurrentPrice());
        auctionSettlementBulkRepository.refundReservedParticipants(reserveParticipations);
    }

    private void endSessionWithoutWinner(
            AuctionSession session,
            LicensePlate plate,
            List<AuctionParticipation> participations
    ) {
        boolean hasConsumedParticipation = participations.stream()
                .anyMatch(p -> p.getStatus().equals(ParticipationStatus.CONSUMED));

        if (hasConsumedParticipation) {
            throw new AppException("Dữ liệu phiên đấu giá không đồng bộ: có người đã bid nhưng không có leader");
        }

        session.setStatus(AuctionSessionStatus.ENDED);
        plate.setStatus(LicensePlateStatus.AVAILABLE);
        List<AuctionParticipation> reservedParticipants = participations.stream()
                .filter(participation -> participation.getStatus() == ParticipationStatus.RESERVED)
                .toList();
        auctionSettlementBulkRepository.refundReservedParticipants(reservedParticipants);
    }

    private void settleFailedSessionRefunds(
            AuctionSession session,
            LicensePlate plate,
            List<AuctionParticipation> participations,
            SessionLifecycleRequest request
    ) {
        String currentLeaderId = session.getCurrentLeaderAccountId();
        boolean isCar = plate.getVehicleType().equals(VehicleType.CAR);
        boolean hasLeader = currentLeaderId != null && !currentLeaderId.isBlank();
        boolean hasConsumerParticipation = participations.stream()
                .anyMatch(p -> p.getStatus().equals(ParticipationStatus.CONSUMED));

        if (session.getStatus().equals(AuctionSessionStatus.SCHEDULED)) {
            if (hasLeader) {
                throw new AppException("Lỗi bất đồng bộ : Tồn tại bidder khi phiên đấu giá chưa bắt đầu");
            }
            if (hasConsumerParticipation) {
                throw new AppException("Dữ liệu không bộ : Phiên đấu giá có người đấu giá nhưng không có leader");
            }
            List<AuctionParticipation> reservedParticipation = participations.stream()
                    .filter(p -> p.getStatus().equals(ParticipationStatus.RESERVED))
                    .toList();
            auctionSettlementBulkRepository.refundReservedParticipants(reservedParticipation);
        } else if (session.getStatus().equals(AuctionSessionStatus.ACTIVE)
                || session.getStatus().equals(AuctionSessionStatus.PAUSED)) {
            if (hasLeader) {
                AuctionParticipation participation = auctionParticipationRepository.findByAuctionSessionIdAndAccountId(session.getId(), currentLeaderId)
                        .orElseThrow(() -> new AppException("Không tìm thấy participation của leader"));

                if (participation.getLastBidAmount() == null
                        || participation.getLastBidAmount().compareTo(session.getCurrentPrice()) != 0) {
                    throw new AppException("Lỗi bất đồng bộ dữ liệu giữa participation và phiên đấu giá");
                }
                walletAtomicRepository.releaseFrozen(currentLeaderId, participation.getLastBidAmount());
                participation.setStatus(ParticipationStatus.REFUNDED);
                auctionParticipationRepository.save(participation);
                List<AuctionParticipation> reservedParticipation = participations.stream()
                        .filter(p -> !currentLeaderId.equals(p.getAccountId()))
                        .filter(p -> p.getStatus().equals(ParticipationStatus.RESERVED))
                        .toList();
                auctionSettlementBulkRepository.refundReservedParticipants(reservedParticipation);
            } else {
                if (hasConsumerParticipation) {
                    throw new AppException("Dữ liệu không bộ : Phiên đấu giá có người đấu giá nhưng không có leader");
                }

                List<AuctionParticipation> reservedParticipation = participations.stream()
                        .filter(p -> p.getStatus().equals(ParticipationStatus.RESERVED))
                        .toList();
                auctionSettlementBulkRepository.refundReservedParticipants(reservedParticipation);
            }

        } else {
            throw new AppException("Phiên đấu giá đang ở trạng thái không hợp lệ để fail");
        }
        session.setStatus(AuctionSessionStatus.FAILED);
        session.setFailureReason(request.getReason());
        plate.setStatus(LicensePlateStatus.AVAILABLE);
        if (isCar) {
            plate.setNextAuctionStartPrice(BigDecimal.valueOf(400000000));
        }
    }

    private void publishSessionStatusChangedEvent(AuctionSession session, EventType eventType) {
        try {
            AuctionSessionRealtimeEvent event = AuctionSessionRealtimeEvent.builder()
                    .auctionSessionId(session.getId())
                    .status(session.getStatus())
                    .type(eventType)
                    .endTime(session.getEndTime())
                    .currentLeaderNameSnapshot(session.getCurrentLeaderNameSnapshot())
                    .currentPrice(session.getCurrentPrice())
                    .occurredAt(LocalDateTime.now())
                    .build();
            auctionSessionRealtimeService.publish(session.getId(), event);
        } catch (Exception e) {
            log.warn("Không thể publish event khi thay đổi trạng thái của phiên đấu giá  với id={}", session.getId());
        }
    }

    private void recordAndPublishStatusChange(
            AuctionSession savedSession,
            AuctionSessionStatus fromStatus,
            String reason,
            StatusChangedByType changedByType,
            EventType eventType
    ) {
        statusHistoryService.recordStatusChange(
                savedSession.getId(),
                fromStatus,
                savedSession.getStatus(),
                reason,
                changedByType,
                null
        );
        publishSessionStatusChangedEvent(savedSession, eventType);
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

    private List<AuctionParticipation> getAllParticipations(String auctionSessionId) {
        return auctionParticipationRepository.findByAuctionSessionId(auctionSessionId);
    }

    private void evictSessionDetailCache(String sessionId) {
        try {
            auctionSessionCacheService.evictSessionDetail(sessionId);
        } catch (Exception e) {
            log.warn("Failed to evict session detail cache. sessionId={}", sessionId, e);
        }
    }
}
