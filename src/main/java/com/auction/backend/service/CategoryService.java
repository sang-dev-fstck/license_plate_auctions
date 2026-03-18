package com.auction.backend.service;

import com.auction.backend.dto.CategoryRequest;
import com.auction.backend.dto.CategoryResponse;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> getAllCategories();

    CategoryResponse addNewCategory(CategoryRequest category);

    CategoryResponse updateCategory(String id, CategoryRequest category);
}
