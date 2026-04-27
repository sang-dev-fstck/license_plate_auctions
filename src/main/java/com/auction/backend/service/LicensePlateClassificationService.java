package com.auction.backend.service;

import com.auction.backend.dto.PlateClassificationResult;

public interface LicensePlateClassificationService {
    PlateClassificationResult classify(String serialNumber);
}
