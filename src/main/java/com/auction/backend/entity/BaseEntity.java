package com.auction.backend.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Getter
@Setter
public abstract class BaseEntity {

    @CreatedDate // Tự động lưu thời gian khi tạo mới
    private LocalDateTime createdAt;

    @LastModifiedDate // Tự động cập nhật thời gian khi có sửa đổi
    private LocalDateTime updatedAt;
}