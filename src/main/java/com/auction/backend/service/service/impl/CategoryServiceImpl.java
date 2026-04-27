package com.auction.backend.service.service.impl;

import com.auction.backend.dto.CategoryRequest;
import com.auction.backend.dto.CategoryResponse;
import com.auction.backend.dto.UpdateCategoryRequest;
import com.auction.backend.entity.Category;
import com.auction.backend.exception.AppException;
import com.auction.backend.mapper.CategoryMapper;
import com.auction.backend.repository.CategoryRepository;
import com.auction.backend.service.CategoryService;
import com.mongodb.DuplicateKeyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
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
            throw new AppException("Category '" + category.getCategoryName() + "' đã tồn tại!");
        }
        Category entity = Category.create(
                category.getCategoryName(),
                category.getRegex(),
                category.getSpecial(),
                category.getPriority()
        );
        Category savedEntity = saveCategoryOrThrowDuplicate(entity);
        return mapper.toResponse(savedEntity);
    }

    @Override
    public CategoryResponse updateCategory(String id, UpdateCategoryRequest category) {
        Category existingCategory = cateRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy category với ID: " + id));
        if (category.getCategoryName() != null
                && !existingCategory.getCategoryName().equals(category.getCategoryName())
                && cateRepo.existsByCategoryName(category.getCategoryName())) {
            throw new AppException("Category '" + category.getCategoryName() + "' đã bị sử dụng bởi một category khác!");
        }
        mapper.updateEntityFromRequest(category, existingCategory);
        Category savedCategory = saveCategoryOrThrowDuplicate(existingCategory);
        return mapper.toResponse(savedCategory);
    }

    private Category saveCategoryOrThrowDuplicate(Category entity) {
        try {
            return cateRepo.save(entity);
        } catch (DuplicateKeyException ex) {
            throw new AppException("Category name already exists");
        }
    }
}
