package com.auction.backend.entity;

import com.auction.backend.enums.TransactionStatus;
import com.auction.backend.enums.TransactionType;
import com.github.f4b6a3.tsid.TsidCreator;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document(collection = "transaction_histories")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionHistory extends BaseEntity {

    @Id
    @EqualsAndHashCode.Include
    @Builder.Default
    private String id = TsidCreator.getTsid().toString();

    @Indexed
    private String walletId; // Giao dịch này của ví nào

    private BigDecimal amount; // Số tiền biến động

    private TransactionType type;

    private TransactionStatus status;

    // CỰC KỲ QUAN TRỌNG ĐỂ TRUY VẾT (AUDIT)
    // Ví dụ: Lưu mã giao dịch của VNPay, hoặc ID của Biển số xe đang đấu giá
    @Indexed
    private String referenceId;

    private String description; // "Nạp tiền qua VNPay", "Đặt cược biển số 30A-999.99"
}
