package com.auction.backend.common;

import com.auction.backend.entity.TagRule;
import com.auction.backend.enums.RuleType;

import java.util.List;

public final class TagRuleMatcher {

    private TagRuleMatcher() {
    }

    public static boolean matches(TagRule rule, String serialNumber) {
        if (rule == null || serialNumber == null || serialNumber.isBlank()) {
            return false;
        }

        RuleType type = rule.getType();
        List<String> values = rule.getValues();

        if (type == null || values == null || values.isEmpty()) {
            return false;
        }

        return switch (type) {
            case ENDS_WITH -> matchesEndsWith(serialNumber, values);
            case NUMERIC_RANGE -> matchesNumericRange(serialNumber, values);
        };
    }

    private static boolean matchesEndsWith(String serialNumber, List<String> values) {
        return values.stream().anyMatch(serialNumber::endsWith);
    }

    private static boolean matchesNumericRange(String serialNumber, List<String> values) {
        if (values.size() < 2 || serialNumber.length() < 4) {
            return false;
        }

        try {
            int start = Integer.parseInt(values.get(0));
            int end = Integer.parseInt(values.get(1));

            String suffix4 = serialNumber.substring(serialNumber.length() - 4);
            int number = Integer.parseInt(suffix4);

            return number >= start && number <= end;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}