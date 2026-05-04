package com.auction.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@Builder
public class PlaceBidResponse {
    private String bidId;
    private String auctionSessionId;
    private String licensePlateNumber;
    private BigDecimal bidAmount;
    private BigDecimal currentPrice;
    private String message;
}