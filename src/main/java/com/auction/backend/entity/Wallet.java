package com.auction.backend.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;

@Document(collection = "wallets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Wallet extends BaseEntity {

    @Id
    private String id;

    @Indexed(unique = true)
    @EqualsAndHashCode.Include
    private String accountId;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal availableBalance;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal frozenBalance;

    private Boolean active;

    @Version
    private Long version;


    // ===== FACTORY METHOD =====
    public static Wallet create(String accountId) {

        return Wallet.builder()
                .accountId(accountId)
                .availableBalance(BigDecimal.ZERO)
                .frozenBalance(BigDecimal.ZERO)
                .active(true)
                .build();
    }

    // ===== DOMAIN METHODS =====

    public void deposit(BigDecimal amount) {
        validateActive();
        validatePositive(amount);
        availableBalance = availableBalance.add(amount);
    }

    public void freeze(BigDecimal amount) {
        validatePositive(amount);

        if (availableBalance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance");
        }

        this.availableBalance = this.availableBalance.subtract(amount);
        this.frozenBalance = this.frozenBalance.add(amount);
    }

    public void unfreeze(BigDecimal amount) {
        validatePositive(amount);

        if (frozenBalance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient frozen balance");
        }

        this.frozenBalance = this.frozenBalance.subtract(amount);
        this.availableBalance = this.availableBalance.add(amount);
    }

    public void debitFrozen(BigDecimal amount) {
        validateActive();
        validatePositive(amount);

        if (frozenBalance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient frozen balance");
        }

        this.frozenBalance = this.frozenBalance.subtract(amount);
    }

    public void withdraw(BigDecimal amount) {
        validateActive();
        validatePositive(amount);

        if (availableBalance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient available balance");
        }

        availableBalance = availableBalance.subtract(amount);
    }
    // ===== VALIDATION =====

    private void validatePositive(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }

    private void validateActive() {
        if (!Boolean.TRUE.equals(active)) {
            throw new IllegalStateException("Wallet is inactive");
        }
    }
}