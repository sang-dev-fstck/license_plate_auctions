package com.auction.backend.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category extends BaseEntity {

    @Id
    private String id;

    @Indexed(unique = true)
    private String categoryName;

    private String regex;

    private Boolean active;

    private Boolean special;

    private Integer priority;
}