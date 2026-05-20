package com.auction.backend.readmodel;

import com.auction.backend.enums.AuctionSessionStatus;
import com.auction.backend.enums.StatusChangedByType;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuctionSessionStatusHistoryReadModel {
    private String id;
    private String auctionSessionId;
    private AuctionSessionStatus fromStatus;
    private AuctionSessionStatus toStatus;
    private String reason;
    private String changedByAccountId;
    private StatusChangedByType changedByType;
}
