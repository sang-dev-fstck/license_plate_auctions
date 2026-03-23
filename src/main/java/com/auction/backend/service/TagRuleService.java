package com.auction.backend.service;

import com.auction.backend.dto.TagRuleRequest;
import com.auction.backend.dto.TagRuleResponse;
import com.auction.backend.dto.UpdateTagRuleRequest;
import com.auction.backend.entity.TagRule;

import java.util.List;

public interface TagRuleService {
    List<TagRule> getAllTagRules();

    TagRuleResponse addTagRule(TagRuleRequest tagRule);

    TagRuleResponse updateTagRule(String id, UpdateTagRuleRequest tagRule);
}
