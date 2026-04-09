package com.auction.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LicensePlateResponse {
    private String id;
    private String plateNumber;
    private String provinceName;
    private String categoryName; // Chỉ hiện tên loại, không cần hiện ID
    private BigDecimal initialPrice;
    private String status;
    private List<String> tags;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime createdAt;
}
