package com.auction.backend.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "categories")
// 1. Tắt callSuper (Không quan tâm class cha)
// 2. Bật chế độ Whitelist (Chỉ so sánh những gì được chỉ định)
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Getter
@Setter
public class Category extends BaseEntity {
    @Id
    private String id;
    @EqualsAndHashCode.Include
    private String categoryName;

    @EqualsAndHashCode.Include
    private String regex;

    @EqualsAndHashCode.Include
    @Field("isSpecial")
    private Boolean special;
}
