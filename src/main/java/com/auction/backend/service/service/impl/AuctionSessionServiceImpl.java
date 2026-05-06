package com.auction.backend.service.service.impl;

import com.auction.backend.dto.AuctionSessionResponse;
import com.auction.backend.dto.CreateAuctionSessionRequest;
import com.auction.backend.dto.CustomerAuctionSessionResponse;
import com.auction.backend.entity.AuctionSession;
import com.auction.backend.entity.AuctionSettings;
import com.auction.backend.entity.LicensePlate;
import com.auction.backend.enums.AuctionSessionStatus;
import com.auction.backend.enums.LicensePlateStatus;
import com.auction.backend.exception.AppException;
import com.auction.backend.mapper.AuctionSessionMapper;
import com.auction.backend.repository.AuctionSessionRepository;
import com.auction.backend.repository.AuctionSettingsRepository;
import com.auction.backend.repository.LicensePlateRepository;
import com.auction.backend.service.AuctionSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionSessionServiceImpl implements AuctionSessionService {
    private final AuctionSessionRepository auctionSessionRepository;
    private final LicensePlateRepository licensePlateRepository;
    private final AuctionSettingsRepository auctionSettingsRepository;
    private final AuctionSessionMapper auctionSessionMapper;

    @Override
    public AuctionSessionResponse createSession(CreateAuctionSessionRequest request) {
        checkTime(request.getStartTime());
        LicensePlate plate = licensePlateRepository.findById(request.getLicensePlateId())
                .orElseThrow(() -> new AppException("Không tìm thấy biển số"));

        if (plate.getStatus() != LicensePlateStatus.AVAILABLE) {
            throw new AppException("Biển số không ở trạng thái có thể tạo phiên đấu giá");
        }

        boolean hasOpenSession = auctionSessionRepository.existsByLicensePlateIdAndStatusIn(
                plate.getId(),
                List.of(
                        AuctionSessionStatus.SCHEDULED,
                        AuctionSessionStatus.ACTIVE,
                        AuctionSessionStatus.PAUSED
                )
        );

        if (hasOpenSession) {
            throw new AppException("Biển số này đã có phiên đấu giá đang tồn tại");
        }

        AuctionSettings setting = auctionSettingsRepository.findByVehicleTypeAndActiveTrue(plate.getVehicleType())
                .orElseThrow(() -> new AppException("Không tìm thấy settings"));

        AuctionSession session = AuctionSession.builder()
                .licensePlateId(plate.getId())
                .licensePlateNumber(plate.getPlateNumber())
                .auctionSettingId(setting.getId())
                .startTime(request.getStartTime())
                .endTime(request.getStartTime().plusHours(1))
                .status(AuctionSessionStatus.SCHEDULED)
                .startingPrice(plate.getNextAuctionStartPrice())
                .currentPrice(plate.getNextAuctionStartPrice())
                .pausedAt(null)
                .remainingSecondsWhenPaused(null)
                .bidStepAmountSnapshot(setting.getBidStepAmount())
                .build();
        AuctionSession savedSession = auctionSessionRepository.save(session);

        try {
            plate.setStatus(LicensePlateStatus.IN_AUCTION);
            licensePlateRepository.save(plate);
        } catch (Exception e) {
            auctionSessionRepository.deleteById(savedSession.getId());
            throw new AppException("Không thể cập nhật trạng thái biển số sau khi tạo phiên đấu giá");
        }

        log.info("Created auctionSessionId={} for plateId={}", savedSession.getId(), plate.getId());
        return auctionSessionMapper.toResponse(savedSession);
    }

    @Override
    public List<CustomerAuctionSessionResponse> getCustomerSessions() {
        List<AuctionSession> sessions = auctionSessionRepository.findByStatusInOrderByStartTimeAsc(
                List.of(
                        AuctionSessionStatus.ACTIVE,
                        AuctionSessionStatus.PAUSED,
                        AuctionSessionStatus.SCHEDULED
                )
        );

        return sessions.stream()
                .map(session -> {
                    LicensePlate plate = licensePlateRepository.findById(session.getLicensePlateId())
                            .orElse(null);

                    return CustomerAuctionSessionResponse.builder()
                            .id(session.getId())
                            .licensePlateNumber(session.getLicensePlateNumber())
                            .categoryName(plate != null ? plate.getCategoryName() : null)
                            .tags(plate != null ? plate.getTags() : List.of())
                            .startTime(session.getStartTime())
                            .endTime(session.getEndTime())
                            .status(session.getStatus())
                            .startingPrice(session.getStartingPrice())
                            .currentPrice(session.getCurrentPrice())
                            .bidStepAmountSnapshot(session.getBidStepAmountSnapshot())
                            .remainingSecondsWhenPaused(null)
                            .build();
                })
                .toList();
    }


    private void checkTime(LocalDateTime startTime) {
        LocalDateTime now = LocalDateTime.now();
        if (startTime.isBefore(now)) {
            throw new AppException("Thời gian không hợp lệ");
        }
    }
}
