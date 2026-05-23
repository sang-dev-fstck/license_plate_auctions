package com.auction.backend.dto;

import com.auction.backend.enums.AuctionSessionStatus;
import com.auction.backend.enums.EventType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuctionSessionRealtimeEvent {
    private EventType type;
    private String auctionSessionId;
    private BigDecimal currentPrice;
    private String currentLeaderAccountId;
    private LocalDateTime endTime;
    private AuctionSessionStatus status;
    private LocalDateTime occurredAt;
}
