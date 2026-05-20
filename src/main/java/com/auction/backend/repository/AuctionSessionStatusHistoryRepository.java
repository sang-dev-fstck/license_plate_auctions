package com.auction.backend.repository;

import com.auction.backend.entity.AuctionSessionStatusHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuctionSessionStatusHistoryRepository extends MongoRepository<AuctionSessionStatusHistory, String> {
    List<AuctionSessionStatusHistory> findByAuctionSessionIdOrderByCreatedAtAsc(String auctionSessionId);
}
