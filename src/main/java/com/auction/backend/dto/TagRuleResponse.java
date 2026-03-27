package com.auction.backend.dto;

import com.auction.backend.enums.RuleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
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
    private boolean active;
}
