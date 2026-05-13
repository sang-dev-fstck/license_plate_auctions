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
public class AuctionSessionDetailResponse {
    private String id;
    private String licensePlateId;
    private String licensePlateNumber;
    private String provinceName;
    private String categoryName;
    private List<String> tags;
    private AuctionSessionStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal startingPrice;
    private BigDecimal currentPrice;
    private BigDecimal bidStepAmountSnapshot;
    private String currentLeaderAccountId;
    private String currentLeaderName;
    private String winnerAccountId;
    private String winnerName;
    private String pauseReason;
    private String failureReason;
}
