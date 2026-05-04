package com.auction.backend.repository;

import com.auction.backend.entity.AuctionParticipation;
import com.auction.backend.enums.ParticipationStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuctionParticipationRepository extends MongoRepository<AuctionParticipation, String> {
    Optional<AuctionParticipation> findByAuctionSessionIdAndAccountIdAndStatus(
            String sessionId,
            String accountId,
            ParticipationStatus status
    );

    Optional<AuctionParticipation> findByAuctionSessionIdAndAccountId(String auctionSessionId, String accountId);

}
