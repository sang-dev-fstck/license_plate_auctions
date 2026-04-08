package com.auction.backend.enums;

public enum TransactionStatus {
    PENDING,    // Đang chờ xử lý (VD: Đang chờ VNPay phản hồi)
    SUCCESS,    // Thành công
    FAILED      // Thất bại
}