package com.auction.backend.service;

import com.auction.backend.dto.*;

import java.util.List;

public interface AuctionSessionQueryService {
    AuctionSessionDetailResponse getSessionDetail(String sessionId);

    List<BidHistoryItemResponse> getBidHistory(String sessionId);

    PageResponse<CustomerAuctionSessionResponse> getCustomerSessions(SearchSessionRequest request);
}
