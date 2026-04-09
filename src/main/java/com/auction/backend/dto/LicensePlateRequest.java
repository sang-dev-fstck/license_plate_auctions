package com.auction.backend.dto;

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
    @NotBlank(message = "Vui lòng chọn Tỉnh/Thành phố")
    private String provinceId;  // Dropdown gửi lên ID: "01" hoặc "HCM"

    // BẮT BUỘC PHẢI CÓ để Utils cắt chuỗi đúng
    // Frontend gửi "CAR" hoặc "MOTORBIKE"
    private String vehicleType;
}
