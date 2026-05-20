package com.auction.backend.entity;

import com.auction.backend.enums.AuctionSessionStatus;
import com.auction.backend.enums.StatusChangedByType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "auction_session_status_histories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

@CompoundIndex(
        name = "idx_session_history_session_created_at",
        def = "{'auctionSessionId': 1, 'createdAt': 1}"
)

public class AuctionSessionStatusHistory extends BaseEntity {
    @Id
    private String id;
    private String auctionSessionId;
    private AuctionSessionStatus fromStatus;
    private AuctionSessionStatus toStatus;
    private String reason;
    private String changedByAccountId;
    private StatusChangedByType changedByType;
}
