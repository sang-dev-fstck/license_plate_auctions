package com.auction.backend.service.service.impl;

import com.auction.backend.dto.TagRuleRequest;
import com.auction.backend.dto.TagRuleResponse;
import com.auction.backend.dto.UpdateTagRuleRequest;
import com.auction.backend.entity.TagRule;
import com.auction.backend.mapper.TagRuleMapper;
import com.auction.backend.repository.TagRuleRepository;
import com.auction.backend.service.TagRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagRuleImpl implements TagRuleService {
    private final TagRuleRepository tagRuleRepository;
    private final TagRuleMapper mapper;

    @Override
    public List<TagRule> getAllTagRules() {
        return tagRuleRepository.findAll();
    }

    @Override
    public TagRuleResponse addTagRule(TagRuleRequest tagRule) {
        if (tagRuleRepository.existsByRuleCode(tagRule.getRuleCode())) {
            throw new RuntimeException("Mã quy tắc '" + tagRule.getRuleCode() + "' đã tồn tại!");
        }

        TagRule entity = mapper.toEntity(tagRule);
        TagRule savedEntity = tagRuleRepository.save(entity);

        return mapper.toResponse(savedEntity);
    }

    @Override
    public TagRuleResponse updateTagRule(String id, UpdateTagRuleRequest tagRule) {
        TagRule existingTagRule = tagRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy quy tắc với ID: " + id));
        TagRule oldSnapshot = mapper.cloneEntity(existingTagRule);
        if (!existingTagRule.getRuleCode().equals(tagRule.getRuleCode())
                && tagRuleRepository.existsByRuleCode(tagRule.getRuleCode())) {
            throw new RuntimeException("Mã quy tắc '" + tagRule.getRuleCode() + "' đã bị sử dụng bởi một quy tắc khác!");
        }
        mapper.updateEntityFromRequest(tagRule, existingTagRule);
        if (existingTagRule.equals(oldSnapshot)) {
            return mapper.toResponse(existingTagRule);
        }
        TagRule savedTagRule = tagRuleRepository.save(existingTagRule);
        return mapper.toResponse(savedTagRule);
    }
}
