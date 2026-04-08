package com.auction.backend.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document(collection = "wallets")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Wallet extends BaseEntity {

    @Id
    private String id;

    @EqualsAndHashCode.Include
    @Indexed(unique = true)
    private String accountId;

    // Số dư khả dụng (Tiền thật sự có thể tiêu)
    // Khởi tạo mặc định là 0 khi vừa tạo ví
    @Builder.Default
    private BigDecimal availableBalance = BigDecimal.ZERO;

    // Số dư đang bị phong tỏa (Đang tham gia đấu giá nhưng chưa chốt kết quả)
    @Builder.Default
    private BigDecimal frozenBalance = BigDecimal.ZERO;

    // Trạng thái ví (Phòng trường hợp phát hiện gian lận, Admin có thể khóa ví, cấm nạp/rút)
    @Builder.Default
    private Boolean isActive = true;

    // VŨ KHÍ BÍ MẬT: Chống "Double Spend" (Tiêu tiền 2 lần cùng lúc)
    // Spring sẽ tự động quản lý con số này, cậu không bao giờ cần set giá trị cho nó.
    @Version
    private Long version;
}
