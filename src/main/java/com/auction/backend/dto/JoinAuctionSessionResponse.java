package com.auction.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@Builder
public class JoinAuctionSessionResponse {
    private String participationId;
    private String auctionSessionId;
    private String licensePlateNumber;
    private BigDecimal depositAmount;
    private String message;
}