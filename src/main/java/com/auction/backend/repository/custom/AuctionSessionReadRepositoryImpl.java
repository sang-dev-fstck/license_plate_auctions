package com.auction.backend.repository.custom;

import com.auction.backend.common.PlateUtils;
import com.auction.backend.dto.SearchSessionRequest;
import com.auction.backend.entity.AuctionSession;
import com.auction.backend.enums.VehicleType;
import com.auction.backend.readmodel.AuctionSessionDetailReadModel;
import com.auction.backend.readmodel.CustomerAuctionSessionReadModel;
import com.auction.backend.repository.AuctionSessionReadRepository;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class AuctionSessionReadRepositoryImpl implements AuctionSessionReadRepository {
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "startTime",
            "endTime",
            "status",
            "licensePlateNumber"
    );
    private final MongoTemplate mongoTemplate;

    @Override
    public Optional<AuctionSessionDetailReadModel> findSessionDetailById(String sessionId) {
        Aggregation aggregation = Aggregation.newAggregation(
                matchSessionById(sessionId),
                lookupCurrentLeader(),
                unwindCurrentLeader(),
                lookupWinner(),
                unwindWinner(),
                lookupLicensePlate(),
                unwindLicensePlate(),
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

    @Override
    public Page<CustomerAuctionSessionReadModel> searchAuctionSessionsDynamic(SearchSessionRequest request) {
        Query query = new Query();

        if (request.getPlateNumber() != null && !request.getPlateNumber().isBlank()) {
            boolean isCar = request.getVehicleType() == VehicleType.CAR;
            String normalizedPlateNumber = PlateUtils.normalizePlateNumber(request.getPlateNumber(), isCar);
            query.addCriteria(
                    Criteria.where("licensePlateNumber").is(normalizedPlateNumber)
            );
        }

        if (request.getStatus() != null) {
            query.addCriteria(Criteria.where("status").is(request.getStatus()));
        }

        if (request.getFromDate() != null || request.getToDate() != null) {
            Criteria startTimeCriteria = Criteria.where("startTime");

            if (request.getFromDate() != null) {
                startTimeCriteria.gte(request.getFromDate());
            }

            if (request.getToDate() != null) {
                startTimeCriteria.lte(request.getToDate());
            }

            query.addCriteria(startTimeCriteria);
        }

        Sort sort = buildSort(request);
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        long total = mongoTemplate.count(query, AuctionSession.class);

        query.with(pageable);

        List<CustomerAuctionSessionReadModel> sessions = mongoTemplate.find(query, CustomerAuctionSessionReadModel.class, "auction_sessions");
        return new PageImpl<>(sessions, pageable, total);
    }

    private Sort buildSort(SearchSessionRequest request) {
        String sortBy = ALLOWED_SORT_FIELDS.contains(request.getSortBy())
                ? request.getSortBy()
                : "startTime";

        Sort.Direction direction = "DESC".equalsIgnoreCase(request.getSortDir())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return Sort.by(direction, sortBy);
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


    private AggregationOperation lookupLicensePlate() {
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

    private AggregationOperation unwindLicensePlate() {
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
                        .append("currentLeaderName", "$currentLeader.fullName")
                        .append("winnerName", "$winner.fullName")
                        .append("pauseReason", 1)
                        .append("failureReason", 1)
        ));
    }

    private AggregationOperation raw(Document document) {
        return context -> document;
    }
}
