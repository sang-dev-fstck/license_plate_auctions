package com.auction.backend.repository;

import com.auction.backend.entity.AuctionParticipation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionParticipationRepository extends MongoRepository<AuctionParticipation, String> {

    Optional<AuctionParticipation> findByAuctionSessionIdAndAccountId(String auctionSessionId, String accountId);

    List<AuctionParticipation> findByAuctionSessionId(String auctionSessionId);

}
