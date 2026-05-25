package com.auction.backend.entity;

import com.auction.backend.enums.AuctionSessionStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "auction_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@CompoundIndexes(value = {
        @CompoundIndex(
                name = "idx_session_status_start_time",
                def = "{'status': 1, 'startTime': 1}"
        ),
        @CompoundIndex(
                name = "idx_session_status_end_time",
                def = "{'status': 1, 'endTime': 1}"
        ),
        @CompoundIndex(
                name = "idx_session_status_plate_start_time",
                def = "{'status': 1, 'licensePlateNumber': 1, 'startTime': 1}"
        )
})

public class AuctionSession extends BaseEntity {

    @Id
    private String id;

    @Indexed
    private String licensePlateId;
    @Indexed
    private String licensePlateNumber;
    private String categoryNameSnapshot;
    private List<String> tags;
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
    private String currentLeaderNameSnapshot;

    private String winnerAccountId;

    private String pauseReason;
    private String failureReason;

    private LocalDateTime pausedAt;
    private Long remainingSecondsWhenPaused;
    private Long reservedCount;
    @Version
    private Long version;
}