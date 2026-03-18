package com.auction.backend.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "provinces")
@Data
public class Province {
    @Id
    private String id;
    @Indexed
    private String provinceName;
    private List<String> codes;
}
