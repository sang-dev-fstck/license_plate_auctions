package com.auction.backend.repository;

import com.auction.backend.entity.AuctionParticipation;

import java.util.List;

public interface AuctionSettlementBulkRepository {
    void refundReservedParticipants(List<AuctionParticipation> reservedParticipations);
}
