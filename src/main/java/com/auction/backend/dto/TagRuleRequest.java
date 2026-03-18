package com.auction.backend.dto;

import com.auction.backend.enums.RuleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class TagRuleRequest {
    @NotBlank(message = "Rule code can not be blank")
    private String ruleCode;
    @NotBlank(message = "Rule name can not be blank")
    private String name;
    @NotBlank(message = "Tag can not be blank")
    private String targetTag;
    @NotNull(message = "Rule type can not be null")
    private RuleType type;
    @NotEmpty(message = "Rule values can not be empty")
    private List<String> values;
    // Mặc định là true, nhưng cho phép Admin gửi false nếu muốn tạo nháp
    private boolean isActive = true;
}
