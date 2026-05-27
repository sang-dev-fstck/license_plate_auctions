package com.auction.backend.dto;

import com.auction.backend.enums.AuctionSessionStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@Setter
@NoArgsConstructor
@Builder
public class AuctionSessionDetailResponse {
    private String id;
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
    private String currentLeaderName;
    private String winnerName;
    private String pauseReason;
    private String failureReason;
}
