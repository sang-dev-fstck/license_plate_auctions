package com.auction.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PlaceBidRequest {

    @NotBlank(message = "Auction session ID không được để trống")
    private String auctionSessionId;

    @NotNull(message = "Số tiền bid không được để trống")
    @DecimalMin(value = "0.01", message = "Số tiền bid phải lớn hơn 0")
    private BigDecimal amount;
}