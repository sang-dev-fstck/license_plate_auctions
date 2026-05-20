package com.auction.backend.repository.custom;

import com.auction.backend.entity.AuctionParticipation;
import com.auction.backend.entity.Wallet;
import com.auction.backend.enums.ParticipationStatus;
import com.auction.backend.exception.AppException;
import com.auction.backend.repository.AuctionSettlementBulkRepository;
import com.mongodb.bulk.BulkWriteResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class AuctionSettlementBulkRepositoryImpl implements AuctionSettlementBulkRepository {
    private final MongoTemplate mongoTemplate;

    @Override
    public void refundReservedParticipants(List<AuctionParticipation> reservedParticipations) {
        if (reservedParticipations == null || reservedParticipations.isEmpty()) {
            log.error("No reserved participation found");
            return;
        }
        BulkOperations walletBulk = mongoTemplate.bulkOps(
                BulkOperations.BulkMode.UNORDERED,
                Wallet.class
        );
        BulkOperations participationBulk = mongoTemplate.bulkOps(
                BulkOperations.BulkMode.UNORDERED,
                AuctionParticipation.class
        );
        for (AuctionParticipation participation : reservedParticipations) {
            BigDecimal depositAmount = participation.getDepositAmount();

            if (depositAmount == null || depositAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new AppException("Dữ liệu tiền đặt cọc không hợp lệ");
            }
            walletBulk.updateOne(
                    Query.query(
                            Criteria.where("accountId").is(participation.getAccountId())
                                    .and("active").is(true)
                                    .and("frozenBalance").gte(depositAmount)
                    ),
                    new Update()
                            .inc("frozenBalance", depositAmount.negate())
                            .inc("availableBalance", depositAmount)
                            .inc("version", 1)
            );
            participationBulk.updateOne(
                    Query.query(
                            Criteria.where("_id").is(participation.getId())
                                    .and("status").is(ParticipationStatus.RESERVED)
                    ),
                    new Update()
                            .set("status", ParticipationStatus.REFUNDED)
            );
        }
        BulkWriteResult walletResult = walletBulk.execute();
        BulkWriteResult participationResult = participationBulk.execute();

        int expected = reservedParticipations.size();

        if (walletResult.getModifiedCount() != expected
                || participationResult.getModifiedCount() != expected) {
            throw new AppException("Không thể hoàn tiền đặt cọc đầy đủ cho người tham gia");
        }
    }
}
