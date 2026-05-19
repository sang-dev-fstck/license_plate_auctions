package com.auction.backend.entity;

import com.auction.backend.enums.ParticipationStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;

@Document(collection = "auction_participations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

@CompoundIndex(
        name = "uk_participation_session_account",
        def = "{'auctionSessionId': 1, 'accountId': 1}",
        unique = true
)
public class AuctionParticipation extends BaseEntity {

    @Id
    private String id;
    private String auctionSessionId;

    private String accountId;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal depositAmount;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal lastBidAmount;

    private ParticipationStatus status;

    @Version
    private Long version;

    public void restoreState(ParticipationStatus status, BigDecimal lastBidAmount) {
        this.status = status;
        this.lastBidAmount = lastBidAmount;
    }
}