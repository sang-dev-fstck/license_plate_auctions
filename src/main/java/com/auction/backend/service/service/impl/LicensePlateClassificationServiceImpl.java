package com.auction.backend.service.service.impl;

import com.auction.backend.common.TagRuleMatcher;
import com.auction.backend.dto.PlateClassificationResult;
import com.auction.backend.entity.Category;
import com.auction.backend.entity.TagRule;
import com.auction.backend.repository.CategoryRepository;
import com.auction.backend.repository.TagRuleRepository;
import com.auction.backend.service.LicensePlateClassificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LicensePlateClassificationServiceImpl implements LicensePlateClassificationService {

    private final CategoryRepository categoryRepository;
    private final TagRuleRepository tagRuleRepository;

    @Override
    public PlateClassificationResult classify(String serialNumber) {
        Category matchedCategory = findCategory(serialNumber);
        List<String> tags = findTags(serialNumber);

        return new PlateClassificationResult(
                matchedCategory != null ? matchedCategory.getId() : null,
                matchedCategory != null ? matchedCategory.getCategoryName() : null,
                tags
        );
    }

    private Category findCategory(String serialNumber) {
        List<Category> categories = categoryRepository.findByActiveTrueOrderByPriorityDesc();

        return categories.stream()
                .filter(category -> matchesCategory(category, serialNumber))
                .findFirst()
                .orElse(null);
    }

    private boolean matchesCategory(Category category, String serialNumber) {
        String regex = category.getRegex();
        if (regex == null || regex.isBlank()) {
            return false;
        }

        try {
            return serialNumber.matches(regex);
        } catch (Exception e) {
            return false;
        }
    }

    private List<String> findTags(String serialNumber) {
        List<TagRule> rules = tagRuleRepository.findByActiveTrue();

        return rules.stream()
                .filter(rule -> TagRuleMatcher.matches(rule, serialNumber))
                .map(TagRule::getTargetTag)
                .filter(tag -> tag != null && !tag.isBlank())
                .distinct()
                .toList();
    }
}
