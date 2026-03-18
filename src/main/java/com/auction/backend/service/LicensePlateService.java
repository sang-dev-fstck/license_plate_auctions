package com.auction.backend.service;

import com.auction.backend.dto.LicensePlateRequest;
import com.auction.backend.dto.LicensePlateResponse;
import com.auction.backend.dto.PageResponse;
import com.auction.backend.dto.PlateSearchRequest;

import java.util.List;

// Đây là bản hợp đồng (Menu)
public interface LicensePlateService {
    List<LicensePlateResponse> getAllPlates();

    LicensePlateResponse addPlate(LicensePlateRequest plate);

    PageResponse<LicensePlateResponse> searchPlates(PlateSearchRequest request);
}
