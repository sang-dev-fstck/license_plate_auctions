package com.auction.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class JoinAuctionSessionRequest {

    @NotBlank(message = "Auction session ID không được để trống")
    private String auctionSessionId;
}