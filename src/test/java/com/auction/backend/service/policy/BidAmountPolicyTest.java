package com.auction.backend.service.policy;

import com.auction.backend.exception.AppException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BidAmountPolicyTest {
    private final BidAmountPolicy bidAmountPolicy = new BidAmountPolicy();

    @Test
    void shouldRejectBidBelowMinimumAllowedAmount() {
        BigDecimal currentPrice = new BigDecimal("100");
        BigDecimal bidStep = new BigDecimal("10");
        BigDecimal submittedAmount = new BigDecimal("109");

        assertThatThrownBy(() ->
                bidAmountPolicy.validate(submittedAmount, currentPrice, bidStep)
        )
                .isInstanceOf(AppException.class)
                .hasMessageContaining("110");
    }

    @ParameterizedTest(name = "should accept submitted amount {0}")
    @CsvSource({
            "110",
            "111",
            "120",
            "110.0",
            "110.00"
    })
    void shouldAcceptBidAtOrAboveMinimumAllowedAmount(
            String submittedAmount
    ) {
        BigDecimal currentPrice = new BigDecimal("100.00");
        BigDecimal bidStep = new BigDecimal("10.00");

        assertThatCode(() ->
                bidAmountPolicy.validate(
                        new BigDecimal(submittedAmount),
                        currentPrice,
                        bidStep
                )
        ).doesNotThrowAnyException();
    }
}
