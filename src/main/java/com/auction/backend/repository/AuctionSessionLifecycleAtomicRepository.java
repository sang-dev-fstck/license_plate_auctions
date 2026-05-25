package com.auction.backend.repository;

import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AuctionSessionLifecycleAtomicRepository {
    boolean claimEnding(String sessionId, LocalDateTime now);
}
