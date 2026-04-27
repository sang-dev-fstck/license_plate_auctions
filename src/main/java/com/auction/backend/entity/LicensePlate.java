package com.auction.backend.entity;

import com.auction.backend.enums.LicensePlateStatus;
import com.auction.backend.enums.VehicleType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.util.List;

@Document(collection = "license_plates")
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Getter
@Setter
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
    @EqualsAndHashCode.Include
    @Indexed(unique = true)
    private String plateNumber;

    // Các trường này đã nằm trong Compound Index nên có thể bỏ @Indexed đơn lẻ
    // để tiết kiệm tài nguyên ghi (Write Performance).
    private String provinceId;
    private String provinceName;

    private String categoryId;
    private String categoryName;
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal nextAuctionStartPrice;
    private String localSymbol;
    private String serialLetter;

    // Search đích danh số đẹp (VD: 55555) -> Vẫn nên giữ Single Index
    @Indexed
    private String serialNumber;
    private LicensePlateStatus status;
    private VehicleType vehicleType;
    private List<String> tags;
}