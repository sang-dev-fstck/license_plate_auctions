package com.auction.backend.entity;

import com.auction.backend.enums.ParticipationStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
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
public class AuctionParticipation extends BaseEntity {

    @Id
    private String id;

    private String auctionSessionId;

    private String accountId;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal depositAmount;

    private ParticipationStatus status;

    @Version
    private Long version;
}