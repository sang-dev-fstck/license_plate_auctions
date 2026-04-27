package com.auction.backend.service.service.impl;

import com.auction.backend.dto.AuctionSessionResponse;
import com.auction.backend.dto.CreateAuctionSessionRequest;
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
        checkTime(request.getStartTime(), request.getEndTime());
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
                .endTime(request.getEndTime())
                .status(AuctionSessionStatus.SCHEDULED)
                .startingPrice(plate.getNextAuctionStartPrice())
                .currentPrice(plate.getNextAuctionStartPrice())
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

    private void checkTime(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime now = LocalDateTime.now();

        if (startTime.isBefore(now) || endTime.isBefore(now)) {
            throw new AppException("Thời gian không hợp lệ");
        }

        if (endTime.isBefore(startTime) || endTime.isEqual(startTime)) {
            throw new AppException("Thời gian kết thúc phải sau thời gian bắt đầu");
        }
    }
}
