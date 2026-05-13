package com.auction.backend.repository;

import com.auction.backend.readmodel.AuctionSessionDetailReadModel;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuctionSessionReadRepository {
    Optional<AuctionSessionDetailReadModel> findSessionDetailById(String sessionId);
}
