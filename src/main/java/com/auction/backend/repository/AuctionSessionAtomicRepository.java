package com.auction.backend.repository;

import com.auction.backend.entity.AuctionSession;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface AuctionSessionAtomicRepository {
    void advanceBid(
            AuctionSession session,
            BigDecimal newAmount,
            String newLeaderAccountId,
            String newLeaderNameSnapshot,
            LocalDateTime newEndTime
    );

    void advanceReserve(AuctionSession session);

    void rollbackReserve(AuctionSession session);
}
