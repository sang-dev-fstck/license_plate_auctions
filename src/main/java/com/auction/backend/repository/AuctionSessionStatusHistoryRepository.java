package com.auction.backend.repository;

import com.auction.backend.entity.AuctionSessionStatusHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuctionSessionStatusHistoryRepository extends MongoRepository<AuctionSessionStatusHistory, String> {
}
