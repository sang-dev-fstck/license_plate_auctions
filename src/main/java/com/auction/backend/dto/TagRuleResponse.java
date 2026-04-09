package com.auction.backend.dto;

import com.auction.backend.enums.RuleType;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagRuleResponse {
    private String id;
    private String ruleCode;
    private String name;
    private String targetTag;
    private RuleType type;
    private List<String> values;
    private Boolean active;
}
