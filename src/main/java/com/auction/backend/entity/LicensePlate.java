package com.auction.backend.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.List;

@Document(collection = "license_plates")
@EqualsAndHashCode(callSuper = true, exclude = {"createdAt", "updatedAt"})
@Data
// --- KHAI BÁO COMPOUND INDEX TẠI ĐÂY ---
@CompoundIndexes({
        // 1. Index cho bộ lọc cơ bản: Trạng thái -> Tỉnh -> Loại
        // Hỗ trợ query: findByStatusAndProvinceIdAndCategoryId...
        @CompoundIndex(name = "filter_basic_idx", def = "{'status': 1, 'provinceId': 1, 'categoryId': 1}"),
        // 2. Index cho tìm kiếm Tags (Phong thủy/Năm sinh): Trạng thái -> Tags
        // Hỗ trợ query: findByStatusAndTags...
        @CompoundIndex(name = "filter_tags_idx", def = "{'status': 1, 'tags': 1}"),
})
public class LicensePlate extends BaseEntity {
    @Id
    private String id;

    // Search đích danh biển số -> Vẫn dùng Single Index Unique
    @Indexed(unique = true)
    private String plateNumber;

    // Các trường này đã nằm trong Compound Index nên có thể bỏ @Indexed đơn lẻ
    // để tiết kiệm tài nguyên ghi (Write Performance).
    private String provinceId;
    private String provinceName;

    private String categoryId;
    private String categoryName;

    private BigDecimal initialPrice;

    private String localSymbol;
    private String serialLetter;

    // Search đích danh số đẹp (VD: 55555) -> Vẫn nên giữ Single Index
    @Indexed
    private String serialNumber;

    private String status;

    private String vehicleType;

    private List<String> tags;
}