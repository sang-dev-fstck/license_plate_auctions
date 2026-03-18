package com.auction.backend.enums;

public enum RuleType {
    CONTAINS,       // Dùng cho "Tránh số": Biển chứa số này -> Dính tag
    ENDS_WITH,      // Dùng cho "Phong thủy": Đuôi biển là số này -> Dính tag
    NUMERIC_RANGE   // Dùng cho "Năm sinh": 4 số cuối nằm trong khoảng này -> Dính tag
}