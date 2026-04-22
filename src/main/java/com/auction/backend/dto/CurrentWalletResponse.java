package com.auction.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrentWalletResponse {
    private String id;
    private String accountId;
    private BigDecimal availableBalance;

    private BigDecimal frozenBalance;

    private Boolean active;
}
