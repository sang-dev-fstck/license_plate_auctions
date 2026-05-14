package com.auction.backend.dto;

import com.auction.backend.enums.AuctionSessionStatus;
import com.auction.backend.enums.VehicleType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SearchSessionRequest {
    private AuctionSessionStatus status = AuctionSessionStatus.ACTIVE;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private String plateNumber;
    private VehicleType vehicleType = VehicleType.CAR;
    @Min(value = 0, message = "Page number is invalid")
    private int page = 0;
    @Min(value = 1, message = "Page size is invalid")
    @Max(value = 100, message = "Page size is invalid")// Mặc định lấy trang đầu tiên (Trang 0)
    private int size = 10;

    private String sortBy = "startTime";
    private String sortDir = "DESC";
    // Mặc định sắp xếp Tăng dần (ASC). Giảm dần là DESC.
}
