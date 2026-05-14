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
public class CustomerAuctionSessionReadModel {
    private String id;
    private String licensePlateNumber;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private AuctionSessionStatus status;
    private BigDecimal startingPrice;
    private BigDecimal currentPrice;
    private BigDecimal bidStepAmountSnapshot;
    private String categoryName;
    private Long remainingSecondsWhenPaused;
    private List<String> tags;
}
