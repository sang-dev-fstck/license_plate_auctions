package com.auction.backend.dto;

import com.auction.backend.enums.AuctionSessionStatus;
import com.auction.backend.enums.StatusChangedByType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class AuctionSessionStatusHistoryResponse {
    private String id;
    private String auctionSessionId;
    private AuctionSessionStatus fromStatus;
    private AuctionSessionStatus toStatus;
    private String reason;
    private String changedByAccountId;
    private StatusChangedByType changedByType;
}
