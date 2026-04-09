package com.auction.backend.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCategoryRequest {

    // Bỏ @NotBlank để cho phép null.
    // Dùng @Size(min = 1) để chặn user cố tình gửi chuỗi rỗng ("") làm hỏng data.
    @Size(min = 1, message = "Category name không được là chuỗi rỗng nếu muốn cập nhật")
    private String categoryName;

    @Size(min = 1, message = "Regex không được là chuỗi rỗng nếu muốn cập nhật")
    private String regex;

    // Chuyển kiểu nguyên thủy 'boolean' thành Object 'Boolean'.
    // Bỏ giá trị mặc định '= true' để nó nhận null khi không được gửi lên.
    private Boolean special;
}