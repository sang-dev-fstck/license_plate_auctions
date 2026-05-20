package com.auction.backend.service;

import com.auction.backend.enums.AuctionSessionStatus;
import com.auction.backend.enums.StatusChangedByType;

public interface AuctionSessionStatusHistoryService {
    void recordStatusChange(
            String sessionId,
            AuctionSessionStatus fromStatus,
            AuctionSessionStatus toStatus,
            String reason,
            StatusChangedByType changedByType,
            String changedByAccountId);
}
