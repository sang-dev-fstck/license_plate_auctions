package com.auction.backend.dto;

import com.auction.backend.enums.AuctionSessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class CustomerAuctionSessionResponse {
    private String id;
    private String licensePlateNumber;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private AuctionSessionStatus status;
    private BigDecimal startingPrice;
    private BigDecimal currentPrice;
    private BigDecimal bidStepAmountSnapshot;
    private String categoryName;
    private List<String> tags;
}