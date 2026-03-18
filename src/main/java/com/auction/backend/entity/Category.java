package com.auction.backend.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "categories")
@EqualsAndHashCode(callSuper = true, exclude = {"createdAt", "updatedAt"})
@Data
public class Category extends BaseEntity {
    @Id
    private String id;
    private String categoryName;
    private String regex;
    private boolean isSpecial;
}
