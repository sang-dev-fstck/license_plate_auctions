package com.auction.backend.controller;

import com.auction.backend.dto.CategoryRequest;
import com.auction.backend.dto.CategoryResponse;
import com.auction.backend.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryResponse> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @PostMapping
    public CategoryResponse addNewCategory(@RequestBody @Valid CategoryRequest request) {
        return categoryService.addNewCategory(request);
    }
}
