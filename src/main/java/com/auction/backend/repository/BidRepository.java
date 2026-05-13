package com.auction.backend.repository;

import com.auction.backend.entity.Bid;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BidRepository extends MongoRepository<Bid, String> {
    List<Bid> findByAuctionSessionIdOrderByCreatedAtDesc(String auctionSessionId);
}