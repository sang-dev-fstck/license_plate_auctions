package com.auction.backend.entity;

import com.auction.backend.enums.RuleType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;


@Document(collection = "tag_rules")
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class TagRule extends BaseEntity {

    @Id
    private String id;

    // Mã quy tắc (Dùng để Admin quản lý, không trùng)
    // VD: "RULE_YEAR_199X", "RULE_AVOID_4"
    @EqualsAndHashCode.Include
    @Indexed(unique = true)
    private String ruleCode;

    // Tên hiển thị cho Admin dễ hiểu
    // VD: "Bộ lọc năm sinh thập niên 90"
    @EqualsAndHashCode.Include
    private String name;

    // Tag sẽ được gắn vào biển số nếu thỏa mãn điều kiện
    // VD: "year_199x", "has_4", "loc_phat"
    @EqualsAndHashCode.Include
    @Indexed
    private String targetTag;

    // Loại logic áp dụng (Quan trọng nhất)
    @EqualsAndHashCode.Include
    private RuleType type;

    // Danh sách giá trị tham chiếu
    // - Nếu type là RANGE: index 0 là min, index 1 là max. VD: ["1990", "1999"]
    // - Nếu type là CONTAINS/ENDS_WITH: là danh sách các số. VD: ["49", "53"] hoặc ["68", "86"]
    @EqualsAndHashCode.Include
    private List<String> values;

    // Bật/Tắt quy tắc này mà không cần xóa
    @Field("isActive") // Giữ liên lạc với DB cũ
    @EqualsAndHashCode.Include
    private Boolean active = true;

}