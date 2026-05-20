package com.auction.backend.repository;

import com.auction.backend.dto.SearchSessionStatusHistoryRequest;
import com.auction.backend.readmodel.AuctionSessionStatusHistoryReadModel;
import org.springframework.data.domain.Page;

public interface AuctionSessionStatusHistoryReadRepository {
    Page<AuctionSessionStatusHistoryReadModel> searchAuctionSessionHistoryDynamic(SearchSessionStatusHistoryRequest request);
}
