package com.auction.backend.repository.custom;

import com.auction.backend.entity.AuctionSession;
import com.auction.backend.enums.AuctionSessionStatus;
import com.auction.backend.repository.AuctionSessionLifecycleAtomicRepository;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class AuctionSessionLifeCycleAtomicRepositoryImpl implements AuctionSessionLifecycleAtomicRepository {
    private final MongoTemplate mongoTemplate;

    @Override
    public boolean claimEnding(String sessionId, LocalDateTime now) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(sessionId));
        query.addCriteria(Criteria.where("status").is(AuctionSessionStatus.ACTIVE));
        query.addCriteria(Criteria.where("endTime").lte(now));

        Update update = new Update()
                .set("status", AuctionSessionStatus.ENDING)
                .inc("version", 1);

        UpdateResult result = mongoTemplate.updateFirst(query, update, AuctionSession.class);
        return result.getModifiedCount() == 1;
    }
}
