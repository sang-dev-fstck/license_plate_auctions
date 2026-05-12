package com.auction.backend.service;

import com.auction.backend.dto.AuctionSessionDetailResponse;
import com.auction.backend.dto.BidHistoryItemResponse;

import java.util.List;

public interface AuctionSessionQueryService {
    AuctionSessionDetailResponse getSessionDetail(String sessionId);

    List<BidHistoryItemResponse> getBidHistory(String sessionId);
}
