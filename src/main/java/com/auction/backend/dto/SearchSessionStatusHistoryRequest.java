package com.auction.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SearchSessionStatusHistoryRequest {
    private String sessionId;
    @Min(value = 0, message = "Page number is invalid")
    private int page = 0;
    @Min(value = 1, message = "Page size is invalid")
    @Max(value = 100, message = "Page size is invalid")// Mặc định lấy trang đầu tiên (Trang 0)
    private int size = 10;

    private String sortBy = "createdAt";
    private String sortDir = "DESC";
}
