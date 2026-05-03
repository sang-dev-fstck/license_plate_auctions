package com.auction.backend.service;

import com.auction.backend.dto.JoinAuctionSessionRequest;
import com.auction.backend.dto.JoinAuctionSessionResponse;

public interface AuctionParticipationService {
    JoinAuctionSessionResponse joinAuctionSession(JoinAuctionSessionRequest request);
}
