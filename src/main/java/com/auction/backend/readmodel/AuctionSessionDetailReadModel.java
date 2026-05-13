package com.auction.backend.readmodel;

import com.auction.backend.enums.AuctionSessionStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuctionSessionDetailReadModel {
    private String id;
    private String licensePlateId;
    private String licensePlateNumber;
    private String categoryName;
    private String provinceName;
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
