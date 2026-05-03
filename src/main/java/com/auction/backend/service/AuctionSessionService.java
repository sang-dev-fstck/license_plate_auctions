package com.auction.backend.service;

import com.auction.backend.dto.AuctionSessionResponse;
import com.auction.backend.dto.CreateAuctionSessionRequest;
import com.auction.backend.dto.CustomerAuctionSessionResponse;

import java.util.List;

public interface AuctionSessionService {
    AuctionSessionResponse createSession(CreateAuctionSessionRequest request);

    List<CustomerAuctionSessionResponse> getCustomerSessions();
}