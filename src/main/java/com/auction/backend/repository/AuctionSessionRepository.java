package com.auction.backend.repository;

import com.auction.backend.entity.AuctionSession;
import com.auction.backend.enums.AuctionSessionStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface AuctionSessionRepository extends MongoRepository<AuctionSession, String> {
    boolean existsByLicensePlateIdAndStatusIn(String licensePlateId, Collection<AuctionSessionStatus> statuses);

    List<AuctionSession> findByStatusInOrderByStartTimeAsc(List<AuctionSessionStatus> statuses);

    List<AuctionSession> findByStatusAndStartTimeLessThanEqualAndEndTimeAfter(AuctionSessionStatus status, LocalDateTime startTime, LocalDateTime endTime);

    List<AuctionSession> findByStatusAndEndTimeLessThanEqual(AuctionSessionStatus status, LocalDateTime endTime);
}
