package com.auction.backend.repository.custom;

import com.auction.backend.entity.AuctionSession;
import com.auction.backend.enums.AuctionSessionStatus;
import com.auction.backend.exception.AppException;
import com.auction.backend.repository.AuctionSessionAtomicRepository;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class AuctionSessionAtomicRepositoryImpl implements AuctionSessionAtomicRepository {
    private final MongoTemplate mongoTemplate;

    @Override
    public void advanceBid(
            AuctionSession session,
            BigDecimal newAmount,
            String newLeaderAccountId,
            String newLeaderNameSnapshot,
            LocalDateTime newEndTime
    ) {
        LocalDateTime now = LocalDateTime.now();

        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(session.getId()));
        query.addCriteria(Criteria.where("status").is(AuctionSessionStatus.ACTIVE));
        query.addCriteria(Criteria.where("version").is(session.getVersion()));
        query.addCriteria(Criteria.where("currentLeaderAccountId").is(session.getCurrentLeaderAccountId()));
        query.addCriteria(Criteria.where("endTime").gt(now));

        // cực kỳ quan trọng: DB currentPrice phải nhỏ hơn bid mới
        query.addCriteria(Criteria.where("currentPrice").lt(newAmount));

        Update update = new Update()
                .set("currentPrice", newAmount)
                .set("currentLeaderAccountId", newLeaderAccountId)
                .set("currentLeaderNameSnapshot", newLeaderNameSnapshot)
                .set("endTime", newEndTime)
                .inc("version", 1);

        UpdateResult result = mongoTemplate.updateFirst(query, update, AuctionSession.class);

        if (result.getModifiedCount() != 1) {
            throw new OptimisticLockingFailureException(
                    "Auction session was updated by another bid"
            );
        }
    }

    @Override
    public void advanceReserve(AuctionSession session) {
        LocalDateTime now = LocalDateTime.now();
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(session.getId()));
        query.addCriteria(Criteria.where("status").is(AuctionSessionStatus.SCHEDULED));
        query.addCriteria(Criteria.where("startTime").gt(now));

        Update update = new Update()
                .inc("reservedCount", 1)
                .inc("version", 1);

        UpdateResult result = mongoTemplate.updateFirst(query, update, AuctionSession.class);

        if (result.getModifiedCount() != 1) {
            throw new AppException(
                    "Auction session has been started by scheduler"
            );
        }
    }

    @Override
    public void rollbackReserve(AuctionSession session) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(session.getId()));
        query.addCriteria(Criteria.where("reservedCount").gt(0));

        Update update = new Update()
                .inc("reservedCount", -1)
                .inc("version", 1);

        UpdateResult result = mongoTemplate.updateFirst(query, update, AuctionSession.class);

        if (result.getModifiedCount() != 1) {
            throw new AppException(
                    "Fail to rollback reserve"
            );
        }
    }

}
