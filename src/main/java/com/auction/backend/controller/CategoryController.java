package com.auction.backend.controller;

import com.auction.backend.dto.CategoryRequest;
import com.auction.backend.dto.CategoryResponse;
import com.auction.backend.dto.UpdateCategoryRequest;
import com.auction.backend.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable String id,
            @RequestBody @Valid UpdateCategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }
}
