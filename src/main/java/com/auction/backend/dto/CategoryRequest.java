package com.auction.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CategoryRequest {
    @NotBlank(message = "Category name can not be blank")
    private String categoryName;
    @NotBlank(message = "Regex can not be blank")
    private String regex;

    private boolean special = true;
}
