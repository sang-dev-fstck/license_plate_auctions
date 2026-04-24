package com.auction.backend.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Document(collection = "provinces")
@Getter
@Setter
public class Province {
    @Id
    private String id;
    @Indexed(unique = true)
    private String provinceName;
    @Indexed
    private Set<String> localSymbols;
}
