package com.auction.backend.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "provinces")
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Province {
    @Id
    private String id;
    @Indexed
    @EqualsAndHashCode.Include
    private String provinceName;
    @EqualsAndHashCode.Include
    private List<String> codes;
}
