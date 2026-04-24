package com.auction.backend.mapper;

import com.auction.backend.dto.CategoryRequest;
import com.auction.backend.dto.CategoryResponse;
import com.auction.backend.dto.UpdateCategoryRequest;
import com.auction.backend.entity.Category;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    Category toEntity(CategoryRequest request);

    CategoryResponse toResponse(Category category);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    // 2. Tuyệt đối KHÔNG BAO GIỜ cho phép update trường ID (Khóa chính)
    @Mapping(target = "id", ignore = true)
    // Nếu entity của bạn có các trường Audit (ngày tạo, người tạo), cũng nên ignore luôn:
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(UpdateCategoryRequest request, @MappingTarget Category entity);
}
