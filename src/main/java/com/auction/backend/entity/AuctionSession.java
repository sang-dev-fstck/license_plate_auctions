package com.auction.backend.entity;

import com.auction.backend.enums.AuctionSessionStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "auction_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionSession extends BaseEntity {

    @Id
    private String id;

    @Indexed
    private String licensePlateId;

    private String licensePlateNumber;

    private String auctionSettingId;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private AuctionSessionStatus status;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal startingPrice;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal currentPrice;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal bidStepAmountSnapshot;

    private String currentLeaderAccountId;
    private String winnerAccountId;

    private String pauseReason;
    private String failureReason;
    private String cancelReason;

    @Version
    private Long version;
}