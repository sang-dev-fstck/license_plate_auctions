package com.auction.backend.entity;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletInfo {
    // Số dư khả dụng (Tiền thật sự có thể tiêu)
    // Khởi tạo mặc định là 0 khi vừa tạo ví
    @Builder.Default
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal availableBalance = BigDecimal.ZERO;

    // Số dư đang bị phong tỏa (Đang tham gia đấu giá nhưng chưa chốt kết quả)
    @Builder.Default
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal frozenBalance = BigDecimal.ZERO;

    // Trạng thái ví (Phòng trường hợp phát hiện gian lận, Admin có thể khóa ví, cấm nạp/rút)
    @Builder.Default
    private Boolean active = true;
}
