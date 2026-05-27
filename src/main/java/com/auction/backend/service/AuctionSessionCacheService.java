package com.auction.backend.service;

public interface AuctionSessionCacheService {
    void evictSessionDetail(String sessionId);

    void evictBidHistory(String sessionId);
}
