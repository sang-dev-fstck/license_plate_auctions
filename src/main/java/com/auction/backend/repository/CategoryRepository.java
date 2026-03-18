package com.auction.backend.repository;

import com.auction.backend.entity.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {
    boolean existsByCategoryName(String categoryName);
    // Tìm category theo mã code (VD: tìm xem NGU_QUY có tồn tại không)
}
