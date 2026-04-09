package com.auction.backend.dto;

import com.auction.backend.enums.RuleType;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTagRuleRequest {

    // Không dùng @NotBlank vì cho phép null (không gửi).
    // Nhưng dùng @Size(min = 1) để chặn trường hợp cố tình gửi chuỗi rỗng: "ruleCode": ""
    @Size(min = 1, message = "Rule code không được là chuỗi rỗng nếu muốn cập nhật")
    private String ruleCode;

    @Size(min = 1, message = "Rule name không được là chuỗi rỗng nếu muốn cập nhật")
    private String name;

    @Size(min = 1, message = "Tag không được là chuỗi rỗng nếu muốn cập nhật")
    private String targetTag;

    // Bỏ @NotNull. Spring Boot sẽ tự động bắt lỗi nếu Frontend gửi sai định dạng của Enum.
    private RuleType type;

    // Bỏ @NotEmpty. Dùng @Size để đảm bảo nếu đã gửi list lên thì không được là list rỗng [].
    @Size(min = 1, message = "Rule values phải có ít nhất 1 phần tử nếu muốn cập nhật")
    private List<String> values;

    // QUAN TRỌNG: Chuyển 'boolean' (kiểu nguyên thủy) thành 'Boolean' (kiểu Object).
    // Bỏ giá trị mặc định 'true' đi.
    private Boolean active;
}