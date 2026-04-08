package com.auction.backend.enums;

public enum TransactionType {
    DEPOSIT,       // Nạp tiền vào ví
    WITHDRAW,      // Rút tiền về ngân hàng
    BID_HOLD,      // Đóng băng tiền khi ra giá
    BID_REFUND,    // Hoàn trả tiền đóng băng (khi có người ra giá cao hơn hoặc thua cuộc)
    BID_DEDUCT     // Trừ hẳn tiền đóng băng (khi thắng đấu giá)
}