package com.auction.backend.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private String id;
    private String categoryName;
    private String regex;
    private Boolean special;
    private Integer priority;
}
