package com.auction.backend.service.service.impl;

import com.auction.backend.dto.CategoryRequest;
import com.auction.backend.dto.CategoryResponse;
import com.auction.backend.entity.Category;
import com.auction.backend.mapper.CategoryMapper;
import com.auction.backend.repository.CategoryRepository;
import com.auction.backend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // <--- Tự động tạo Constructor cho các biến final
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository cateRepo;
    private final CategoryMapper mapper;

    @Override
    public List<CategoryResponse> getAllCategories() {
        return cateRepo.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponse addNewCategory(CategoryRequest category) {
        if (cateRepo.existsByCategoryName(category.getCategoryName())) {
            throw new RuntimeException("Category '" + category.getCategoryName() + "' đã tồn tại!");
        }
        Category entity = mapper.toEntity(category);
        cateRepo.save(entity);
        return mapper.toResponse(entity);
    }

    @Override
    public CategoryResponse updateCategory(String id, CategoryRequest category) {
        Category existingCategory = cateRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy category với ID: " + id));
        Category oldSnapshot = new Category();
        BeanUtils.copyProperties(existingCategory, oldSnapshot);
        if (!existingCategory.getCategoryName().equals(category.getCategoryName())
                && cateRepo.existsByCategoryName(category.getCategoryName())) {
            throw new RuntimeException("Category '" + category.getCategoryName() + "' đã bị sử dụng bởi một category khác!");
        }
        mapper.updateEntityFromRequest(category, existingCategory);
        if (existingCategory.equals(oldSnapshot)) {
            throw new IllegalArgumentException("Dữ liệu không có thay đổi nào so với hiện tại");
        }
        Category savedCategory = cateRepo.save(existingCategory);
        return mapper.toResponse(savedCategory);
    }
}
