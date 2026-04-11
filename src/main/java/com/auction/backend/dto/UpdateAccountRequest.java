package com.auction.backend.dto;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAccountRequest {

    private String fullName;

    // Regex: Bắt đầu bằng 0, theo sau là 9 chữ số (Chuẩn mạng VN)
    @Pattern(regexp = "^(0)[0-9]{9}$",
            message = "Số điện thoại phải bao gồm 10 chữ số và bắt đầu bằng số 0")
    private String phoneNumber;
}
