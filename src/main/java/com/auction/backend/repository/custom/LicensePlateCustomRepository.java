package com.auction.backend.repository.custom;

import com.auction.backend.dto.PlateSearchRequest;
import com.auction.backend.entity.LicensePlate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class LicensePlateCustomRepository {
    private final MongoTemplate mongoTemplate; // Vũ khí hạng nặng của MongoDB trong Spring


    public Page<LicensePlate> searchDynamic(PlateSearchRequest request) {
        Query query = new Query();
        if (request.getStatus() != null) {
            query.addCriteria(Criteria.where("status").is(request.getStatus()));
        }
        if (request.getProvinceName() != null && !request.getProvinceName().isEmpty()) {
            query.addCriteria((Criteria.where("provinceName").is(request.getProvinceName())));
        }
        if (request.getCategoryId() != null && !request.getCategoryId().isEmpty()) {
            query.addCriteria(Criteria.where("categoryId").is(request.getCategoryId()));
        }
        // 4. Nếu người dùng chọn Tags (TÌM KIẾM MỀM - SỞ THÍCH)
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            // Toán tử $all trong MongoDB: Biển số phải chứa TẤT CẢ các tags người dùng chọn
            query.addCriteria(Criteria.where("tags").all(request.getTags()));
        }
        // --- 2. ĐẾM TỔNG SỐ BẢN GHI (Trông DB có bao nhiêu cái khớp điều kiện) ---
        long total = mongoTemplate.count(query, LicensePlate.class);
        // --- 3. CẤU HÌNH SẮP XẾP VÀ PHÂN TRANG ---
        Sort sort = request.getSortDir().equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(request.getSortBy()).ascending() :
                Sort.by(request.getSortBy()).descending();
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        query.with(pageable);

        List<LicensePlate> plates = mongoTemplate.find(query, LicensePlate.class);
        return new PageImpl<>(plates, pageable, total);
    }
}
