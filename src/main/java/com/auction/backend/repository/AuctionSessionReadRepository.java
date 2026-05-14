package com.auction.backend.repository;

import com.auction.backend.dto.SearchSessionRequest;
import com.auction.backend.readmodel.AuctionSessionDetailReadModel;
import com.auction.backend.readmodel.CustomerAuctionSessionReadModel;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuctionSessionReadRepository {
    Optional<AuctionSessionDetailReadModel> findSessionDetailById(String sessionId);

    Page<CustomerAuctionSessionReadModel> searchAuctionSessionsDynamic(SearchSessionRequest request);
}
