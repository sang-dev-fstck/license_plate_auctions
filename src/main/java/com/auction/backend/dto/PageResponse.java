package com.auction.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse<T> {
    private List<T> data; // Chứa danh sách biển số
    private PaginationMeta meta; // Chứa thông tin phân trang

    // --- THÊM HÀM STATIC NÀY VÀO ĐÂY ---
    // Hàm này sẽ tự động đọc object Page của Spring và build ra PaginationMeta
    public static <T> PageResponse<T> of(Page<?> springPage, List<T> mappedData) {
        PaginationMeta metaInfo = PaginationMeta.builder()
                .pageNo(springPage.getNumber())
                .pageSize(springPage.getSize())
                .totalElements(springPage.getTotalElements())
                .totalPages(springPage.getTotalPages())
                .isLast(springPage.isLast())
                .build();

        return new PageResponse<>(mappedData, metaInfo);
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationMeta {
        private int pageNo;
        private int pageSize;
        private long totalElements;
        private int totalPages;
        private boolean isLast;
    }      // Có phải trang cuối cùng không?
}
