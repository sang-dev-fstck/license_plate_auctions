package com.auction.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryRequest {
    @NotBlank(message = "Category name can not be blank")
    private String categoryName;
    @NotBlank(message = "Regex can not be blank")
    private String regex;

    private Boolean special = true;
}
