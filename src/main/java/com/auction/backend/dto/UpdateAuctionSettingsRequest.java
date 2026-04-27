package com.auction.backend.dto;

import com.auction.backend.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class UpdateAuctionSettingsRequest {
    private BigDecimal bidStepAmount;
    private VehicleType vehicleType;
    private Boolean active;
}
