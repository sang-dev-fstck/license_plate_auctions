package com.auction.backend.entity;

import com.auction.backend.enums.VehicleType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;

@Document(collection = "auction_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class AuctionSettings extends BaseEntity {

    @Id
    @EqualsAndHashCode.Include
    private String id;

    @Indexed(unique = true)
    private VehicleType vehicleType;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal bidStepAmount;

    private Boolean active;
}