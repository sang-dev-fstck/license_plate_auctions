package com.auction.backend.dto;

import com.auction.backend.enums.VehicleType;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuctionSettingsResponse {
    private String id;
    private BigDecimal bidStepAmount;
    private VehicleType vehicleType;
    private Boolean active;
}
