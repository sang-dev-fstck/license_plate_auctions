package com.auction.backend.repository.custom;

import com.auction.backend.readmodel.AuctionSessionDetailReadModel;
import com.auction.backend.repository.AuctionSessionReadRepository;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AuctionSessionReadRepositoryImpl implements AuctionSessionReadRepository {
    private final MongoTemplate mongoTemplate;

    @Override
    public Optional<AuctionSessionDetailReadModel> findSessionDetailById(String sessionId) {
        Aggregation aggregation = Aggregation.newAggregation(
                matchSessionById(sessionId),
                lookupCurrentLeader(),
                unwindCurrentLeader(),
                lookupWinner(),
                unwindWinner(),
                lookupPlateNumber(),
                unwindPlateNumber(),
                projectSessionDetail()
        );
        AggregationResults<AuctionSessionDetailReadModel> results =
                mongoTemplate.aggregate(
                        aggregation,
                        "auction_sessions",
                        AuctionSessionDetailReadModel.class
                );
        return results.getMappedResults()
                .stream()
                .findFirst();
    }

    private AggregationOperation matchSessionById(String sessionId) {
        return raw(new Document("$match",
                new Document("$expr",
                        new Document("$eq", java.util.List.of(
                                new Document("$toString", "$_id"),
                                sessionId
                        ))
                )
        ));
    }

    private AggregationOperation lookupCurrentLeader() {
        return raw(new Document("$lookup",
                new Document("from", "accounts")
                        .append("let", new Document("leaderId", "$currentLeaderAccountId"))
                        .append("pipeline", java.util.List.of(
                                new Document("$match",
                                        new Document("$expr",
                                                new Document("$eq", java.util.List.of(
                                                        new Document("$toString", "$_id"),
                                                        "$$leaderId"
                                                ))
                                        )
                                ),
                                new Document("$project",
                                        new Document("fullName", 1)
                                                .append("_id", 0)
                                )
                        ))
                        .append("as", "currentLeader")
        ));
    }

    private AggregationOperation unwindCurrentLeader() {
        return raw(new Document("$unwind",
                new Document("path", "$currentLeader")
                        .append("preserveNullAndEmptyArrays", true)
        ));
    }

    private AggregationOperation lookupWinner() {
        return raw(new Document("$lookup",
                new Document("from", "accounts")
                        .append("let", new Document("winnerId", "$winnerAccountId"))
                        .append("pipeline", java.util.List.of(
                                new Document("$match",
                                        new Document("$expr",
                                                new Document("$eq", java.util.List.of(
                                                        new Document("$toString", "$_id"),
                                                        "$$winnerId"
                                                ))
                                        )
                                ),
                                new Document("$project",
                                        new Document("fullName", 1)
                                                .append("_id", 0)
                                )
                        ))
                        .append("as", "winner")
        ));
    }


    private AggregationOperation lookupPlateNumber() {
        return raw(new Document("$lookup",
                new Document("from", "license_plates")
                        .append("let", new Document("plateNumberId", "$licensePlateId"))
                        .append("pipeline", java.util.List.of(
                                new Document("$match",
                                        new Document("$expr",
                                                new Document("$eq", java.util.List.of(
                                                        new Document("$toString", "$_id"),
                                                        "$$plateNumberId"
                                                ))
                                        )
                                ),
                                new Document("$project",
                                        new Document("categoryName", 1)
                                                .append("tags", 1)
                                                .append("provinceName", 1)
                                                .append("_id", 0)
                                )
                        ))
                        .append("as", "licensePlate")
        ));
    }

    private AggregationOperation unwindPlateNumber() {
        return raw(new Document("$unwind",
                new Document("path", "$licensePlate")
                        .append("preserveNullAndEmptyArrays", true)
        ));
    }

    private AggregationOperation unwindWinner() {
        return raw(new Document("$unwind",
                new Document("path", "$winner")
                        .append("preserveNullAndEmptyArrays", true)
        ));
    }

    private AggregationOperation projectSessionDetail() {
        return raw(new Document("$project",
                new Document("id", new Document("$toString", "$_id"))
                        .append("licensePlateId", 1)
                        .append("licensePlateNumber", 1)
                        .append("provinceName", "$licensePlate.provinceName")
                        .append("categoryName", "$licensePlate.categoryName")
                        .append("tags", "$licensePlate.tags")
                        .append("status", 1)
                        .append("startTime", 1)
                        .append("endTime", 1)
                        .append("startingPrice", 1)
                        .append("currentPrice", 1)
                        .append("bidStepAmountSnapshot", 1)
                        .append("currentLeaderAccountId", 1)
                        .append("currentLeaderName", "$currentLeader.fullName")
                        .append("winnerAccountId", 1)
                        .append("winnerName", "$winner.fullName")
                        .append("pauseReason", 1)
                        .append("failureReason", 1)
        ));
    }

    private AggregationOperation raw(Document document) {
        return context -> document;
    }
}
