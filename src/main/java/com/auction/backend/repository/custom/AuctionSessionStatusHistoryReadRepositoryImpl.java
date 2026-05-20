package com.auction.backend.repository.custom;

import com.auction.backend.dto.SearchSessionStatusHistoryRequest;
import com.auction.backend.entity.AuctionSessionStatusHistory;
import com.auction.backend.readmodel.AuctionSessionStatusHistoryReadModel;
import com.auction.backend.repository.AuctionSessionStatusHistoryReadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class AuctionSessionStatusHistoryReadRepositoryImpl implements AuctionSessionStatusHistoryReadRepository {
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "createdAt"
    );
    private final MongoTemplate mongoTemplate;

    @Override
    public Page<AuctionSessionStatusHistoryReadModel> searchAuctionSessionHistoryDynamic(SearchSessionStatusHistoryRequest request) {
        Query query = new Query();

        if (request.getSessionId() != null && !request.getSessionId().isBlank()) {
            query.addCriteria(Criteria.where("auctionSessionId").is(request.getSessionId()));
        }
        
        long total = mongoTemplate.count(query, AuctionSessionStatusHistory.class);

        Sort sort = buildSort(request);

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        query.with(pageable);

        List<AuctionSessionStatusHistoryReadModel> histories = mongoTemplate.find(
                query, AuctionSessionStatusHistoryReadModel.class, "auction_session_status_histories");
        return new PageImpl<>(histories, pageable, total);
    }

    private Sort buildSort(SearchSessionStatusHistoryRequest request) {
        String sortBy = ALLOWED_SORT_FIELDS.contains(request.getSortBy())
                ? request.getSortBy()
                : "createdAt";

        Sort.Direction direction = "DESC".equalsIgnoreCase(request.getSortDir())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return Sort.by(direction, sortBy);
    }
}
