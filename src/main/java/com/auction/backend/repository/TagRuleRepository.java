package com.auction.backend.repository;

import com.auction.backend.entity.TagRule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRuleRepository extends MongoRepository<TagRule, String> {
    boolean existsByRuleCode(String ruleCode);
    // Lấy hết luật ra để chạy vòng lặp kiểm tra
    // (Vì số lượng luật thường ít, < 100 luật, nên lấy all không sao)
}
