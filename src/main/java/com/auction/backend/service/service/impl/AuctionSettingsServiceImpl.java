package com.auction.backend.service.service.impl;

import com.auction.backend.dto.AuctionSettingsRequest;
import com.auction.backend.dto.AuctionSettingsResponse;
import com.auction.backend.dto.UpdateAuctionSettingsRequest;
import com.auction.backend.entity.AuctionSettings;
import com.auction.backend.exception.AppException;
import com.auction.backend.mapper.AuctionSettingsMapper;
import com.auction.backend.repository.AuctionSettingsRepository;
import com.auction.backend.service.AuctionSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionSettingsServiceImpl implements AuctionSettingsService {
    private final AuctionSettingsRepository auctionSettingsRepository;
    private final AuctionSettingsMapper auctionSettingsMapper;

    @Override
    public List<AuctionSettingsResponse> getAuctionSettings() {
        return auctionSettingsRepository.findAll()
                .stream()
                .map(auctionSettingsMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AuctionSettingsResponse getAuctionSettingById(String id) {
        if (id == null || id.isBlank()) {
            throw new AppException("AuctionSettings id must not be blank");
        }
        AuctionSettings auctionSettings = auctionSettingsRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("AuctionSettings with id " + id + " not found"));
        log.info("Getting AuctionSettings with id {}", auctionSettings.getId());
        return auctionSettingsMapper.toResponse(auctionSettings);
    }

    @Override
    public AuctionSettingsResponse addAuctionSetting(AuctionSettingsRequest request) {
        AuctionSettings auctionSettings = auctionSettingsMapper.toEntity(request);
        auctionSettings.setActive(true);
        try {
            AuctionSettings addedEntity = auctionSettingsRepository.save(auctionSettings);
            return auctionSettingsMapper.toResponse(addedEntity);
        } catch (DuplicateKeyException e) {
            throw AppException.conflict("vehicleType", "Setting cho loại xe này đã tồn tại");
        } catch (Exception e) {
            log.error("Failed to create auction setting. vehicleType={}", request.getVehicleType(), e);
            throw new AppException("Không thể tạo auction setting, vui lòng thử lại");
        }
    }

    @Override
    public AuctionSettingsResponse updateAuctionSetting(UpdateAuctionSettingsRequest request, String id) {
        AuctionSettings existingSettings = auctionSettingsRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("Không tìm thấy setting"));
        auctionSettingsMapper.updateEntityFromRequest(request, existingSettings);
        AuctionSettings updatedSettings = auctionSettingsRepository.save(existingSettings);
        return auctionSettingsMapper.toResponse(updatedSettings);
    }
}
