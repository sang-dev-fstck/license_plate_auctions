package com.auction.backend.service;

import com.auction.backend.dto.AuctionSessionResponse;
import com.auction.backend.dto.CreateAuctionSessionRequest;

public interface AuctionSessionService {
    AuctionSessionResponse createSession(CreateAuctionSessionRequest request);
}