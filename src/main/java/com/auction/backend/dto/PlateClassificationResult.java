package com.auction.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PlateClassificationResult {
    private String categoryId;
    private String categoryName;
    private List<String> tags;
}
