package com.auction.backend.common;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class MoneyUtils {

    private static final Locale VI_LOCALE = Locale.forLanguageTag("vi-VN");

    public static String format(BigDecimal amount) {
        return NumberFormat.getInstance(VI_LOCALE).format(amount);
    }
}
