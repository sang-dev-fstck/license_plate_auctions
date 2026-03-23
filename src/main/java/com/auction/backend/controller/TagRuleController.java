package com.auction.backend.controller;

import com.auction.backend.dto.TagRuleRequest;
import com.auction.backend.dto.TagRuleResponse;
import com.auction.backend.dto.UpdateTagRuleRequest;
import com.auction.backend.entity.TagRule;
import com.auction.backend.service.TagRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
public class TagRuleController {
    private final TagRuleService tagRuleService;

    @GetMapping
    public List<TagRule> getAllTagRules() {
        return tagRuleService.getAllTagRules();
    }

    @PostMapping
    public TagRuleResponse addTagRule(@RequestBody @Valid TagRuleRequest tagRule) {
        return tagRuleService.addTagRule(tagRule);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TagRuleResponse> updateRule(
            @PathVariable String id,
            @RequestBody @Valid UpdateTagRuleRequest request) {

        return ResponseEntity.ok(tagRuleService.updateTagRule(id, request));
    }
}
