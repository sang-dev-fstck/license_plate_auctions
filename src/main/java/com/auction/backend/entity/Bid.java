package com.auction.backend.entity;

import com.auction.backend.enums.BidStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;

@Document(collection = "bids")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bid extends BaseEntity {

    @Id
    private String id;

    private String auctionSessionId;
    
    private String bidderAccountId;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal amount;

    private BidStatus status;

    @Version
    private Long version;
}