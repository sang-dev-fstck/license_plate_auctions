package com.auction.backend.service.service.impl;

import com.auction.backend.common.PlateUtils;
import com.auction.backend.dto.LicensePlateRequest;
import com.auction.backend.dto.LicensePlateResponse;
import com.auction.backend.dto.PageResponse;
import com.auction.backend.dto.PlateSearchRequest;
import com.auction.backend.entity.LicensePlate;
import com.auction.backend.mapper.LicensePlateMapper;
import com.auction.backend.repository.LicensePlateRepository;
import com.auction.backend.repository.custom.LicensePlateCustomRepository;
import com.auction.backend.service.LicensePlateService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LicensePlateServiceImpl implements LicensePlateService {
    private final LicensePlateRepository licensePlateRepository;
    private final LicensePlateCustomRepository customRepository;
    private final LicensePlateMapper mapper;

    @Override
    public List<LicensePlateResponse> getAllPlates() {
        // Service query DB và tự map sang Response DTO luôn
        return licensePlateRepository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public LicensePlateResponse addPlate(LicensePlateRequest plate) {
        boolean isCar = "CAR".equalsIgnoreCase(plate.getVehicleType());
        String serial = PlateUtils.extractSerialNumber(plate.getPlateNumber(), isCar);
        String localSymbol = PlateUtils.extractLocalSymbol(plate.getPlateNumber());
        if (serial.isEmpty()) {
            throw new RuntimeException("Biển số không hợp lệ !");
        }
        if (localSymbol.isEmpty()) {
            throw new RuntimeException("Biển số không hợp lệ !");
        }
        LicensePlate entity = mapper.toEntity(plate);
        entity.setStatus("AVAILABLE");
        // Ép cứng giá khởi điểm mặc định ban đầu là 40 triệu
        entity.setInitialPrice(new BigDecimal("40000000"));
        entity.setSerialNumber(serial);
        entity.setLocalSymbol(localSymbol);
        LicensePlate savedEntity = licensePlateRepository.save(entity);
        return mapper.toResponse(savedEntity);
    }

    @Override
    public PageResponse<LicensePlateResponse> searchPlates(PlateSearchRequest request) {
        Page<LicensePlate> pageResult = customRepository.searchDynamic(request);
        // 2. Map từ Entity sang DTO Response
        List<LicensePlateResponse> content = pageResult.getContent().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        return PageResponse.of(pageResult, content);
    }
}
