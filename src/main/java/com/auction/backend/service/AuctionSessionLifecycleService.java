package com.auction.backend.service;

import com.auction.backend.dto.AuctionSessionResponse;
import com.auction.backend.dto.SessionLifecycleRequest;

public interface AuctionSessionLifecycleService {
    AuctionSessionResponse activateSession(String sessionId);

    AuctionSessionResponse pauseOrResumeSession(String sessionId, SessionLifecycleRequest request);

    AuctionSessionResponse failSession(String sessionId, SessionLifecycleRequest request);
}
