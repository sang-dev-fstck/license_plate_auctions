package com.auction.backend.service.policy;

import com.auction.backend.common.MoneyUtils;
import com.auction.backend.exception.AppException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;

@Component
public class BidAmountPolicy {
    public void validate(
            BigDecimal submittedAmount,
            BigDecimal currentPrice,
            BigDecimal bidStep
    ) {
        Objects.requireNonNull(submittedAmount, "submittedAmount must not be null");
        Objects.requireNonNull(currentPrice, "currentPrice must not be null");
        Objects.requireNonNull(bidStep, "bidStep must not be null");


        BigDecimal minimumAmount = currentPrice.add(bidStep);

        if (submittedAmount.compareTo(minimumAmount) < 0) {
            throw new AppException(String.format(
                    "Giá đặt phải ≥ %s VNĐ",
                    MoneyUtils.format(minimumAmount)
            ));
        }
    }
}
