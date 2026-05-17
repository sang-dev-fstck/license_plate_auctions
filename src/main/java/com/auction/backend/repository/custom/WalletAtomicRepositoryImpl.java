package com.auction.backend.repository.custom;

import com.auction.backend.exception.AppException;
import com.auction.backend.repository.WalletAtomicRepository;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
@RequiredArgsConstructor
public class WalletAtomicRepositoryImpl implements WalletAtomicRepository {
    private final MongoTemplate mongoTemplate;

    @Override
    public void freezeAvailable(String accountId, BigDecimal amount) {
        validatePositive(amount);

        Query query = new Query();
        query.addCriteria(Criteria.where("accountId").is(accountId));
        query.addCriteria(Criteria.where("active").is(true));
        query.addCriteria(Criteria.where("availableBalance").gte(amount));

        Update update = new Update()
                .inc("availableBalance", amount.negate())
                .inc("frozenBalance", amount)
                .inc("version", 1);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, "wallets");

        if (updateResult.getModifiedCount() != 1) {
            throw new AppException("Số dư đang khóa không đủ");
        }
    }

    @Override
    public void releaseFrozen(String accountId, BigDecimal amount) {
        validatePositive(amount);
        Query query = new Query();
        query.addCriteria(Criteria.where("accountId").is(accountId));
        query.addCriteria(Criteria.where("active").is(true));
        query.addCriteria(Criteria.where("frozenBalance").gte(amount));

        Update update = new Update()
                .inc("frozenBalance", amount.negate())
                .inc("availableBalance", amount)
                .inc("version", 1);

        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, "wallets");

        if (updateResult.getModifiedCount() != 1) {
            throw new AppException("Số dư đang khóa không đủ");
        }
    }

    @Override
    public void debitFrozen(String accountId, BigDecimal amount) {
        validatePositive(amount);
        Query query = new Query();
        query.addCriteria(Criteria.where("accountId").is(accountId));
        query.addCriteria(Criteria.where("active").is(true));
        query.addCriteria(Criteria.where("frozenBalance").gte(amount));

        Update update = new Update()
                .inc("frozenBalance", amount.negate())
                .inc("version", 1);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, "wallets");

        if (updateResult.getModifiedCount() != 1) {
            throw new AppException("Số dư đang khóa không đủ để thanh toán");
        }
    }

    @Override
    public void creditAvailable(String accountId, BigDecimal amount) {
        validatePositive(amount);
        Query query = new Query();
        query.addCriteria(Criteria.where("accountId").is(accountId));
        query.addCriteria(Criteria.where("active").is(true));

        Update update = new Update()
                .inc("availableBalance", amount)
                .inc("version", 1);

        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, "wallets");
        if (updateResult.getModifiedCount() != 1) {
            throw new AppException("Không tìm thấy ví hoặc ví đã bị khóa");
        }

    }

    private void validatePositive(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException("Số tiền phải lớn hơn 0");
        }
    }
}
