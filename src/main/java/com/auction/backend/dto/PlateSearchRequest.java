package com.auction.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class PlateSearchRequest {
    private String provinceName;
    private String categoryId;

    // VD: Người dùng muốn tìm biển có chữ số "9" và thuộc năm sinh "199x"
    // Họ sẽ gửi lên list: ["has_9", "year_199x"]
    private List<String> tags;

    // Mặc định luôn chỉ tìm xe đang mở bán
    private String status = "AVAILABLE";

    // --- CÁC THÔNG SỐ PHÂN TRANG & SẮP XẾP (Có giá trị mặc định) ---
    @Min(value = 0, message = "Page number is invalid")
    private int page = 0;
    @Min(value = 0, message = "Page size is invalid")
    @Max(value = 100, message = "Page size is invalid")// Mặc định lấy trang đầu tiên (Trang 0)
    private int size = 10;                  // Mặc định lấy 10 bản ghi / trang
    private String sortBy = "initialPrice"; // Mặc định sắp xếp theo giá
    private String sortDir = "ASC";         // Mặc định sắp xếp Tăng dần (ASC). Giảm dần là DESC.
}
