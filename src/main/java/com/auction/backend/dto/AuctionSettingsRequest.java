package com.auction.backend.dto;

import com.auction.backend.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AuctionSettingsRequest {
    private BigDecimal bidStepAmount;
    private VehicleType vehicleType;
}
