package com.auction.backend.dto;

import com.auction.backend.enums.BidStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class BidHistoryItemResponse {
    private String bidId;
    private String bidderAccountId;
    private String bidderName;
    private BigDecimal amount;
    private BidStatus status;
    private LocalDateTime createdAt;
}
