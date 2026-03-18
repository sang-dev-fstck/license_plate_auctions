package com.auction.backend.controller;

import com.auction.backend.dto.LicensePlateRequest;
import com.auction.backend.dto.LicensePlateResponse;
import com.auction.backend.dto.PageResponse;
import com.auction.backend.dto.PlateSearchRequest;
import com.auction.backend.mapper.LicensePlateMapper;
import com.auction.backend.service.LicensePlateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/plates")
@RequiredArgsConstructor
public class LicensePlateController {
    private final LicensePlateService licensePlateService;
    private final LicensePlateMapper licensePlateMapper;

    // Trong LicensePlateController.java
    @GetMapping
    public List<LicensePlateResponse> getLicensePlates() {
        // Controller mỏng nhẹ, sạch sẽ
        return licensePlateService.getAllPlates();
    }

    @PostMapping
    public LicensePlateResponse addLicensePlate(@RequestBody @Valid LicensePlateRequest request) {
        // @RequestBody: Spring tự động biến JSON người dùng gửi thành object Java
        // 1. Convert Request -> Entity
        return licensePlateService.addPlate(request);
    }

    @PostMapping("/search")
    public PageResponse<LicensePlateResponse> searchLicensePlates(@RequestBody PlateSearchRequest request) {
        return licensePlateService.searchPlates(request);
    }
}
