package com.auction.backend.dto;

import com.auction.backend.enums.VehicleType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LicensePlateRequest {
    @NotBlank(message = "Plate number can not be blank")
    private String plateNumber;

    // BẮT BUỘC PHẢI CÓ để Utils cắt chuỗi đúng
    // Frontend gửi "CAR" hoặc "MOTORBIKE"
    private VehicleType vehicleType;
}
