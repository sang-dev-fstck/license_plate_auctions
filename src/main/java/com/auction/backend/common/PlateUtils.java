package com.auction.backend.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PlateUtils {

    private static final Pattern NUMERIC_SUFFIX_PATTERN = Pattern.compile("\\d+$");

    private PlateUtils() {
    }

    private static String clean(String rawInput) {
        if (rawInput == null || rawInput.isBlank()) {
            return "";
        }
        return rawInput.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
    }

    /**
     * Trả về serial number dùng cho classify/search.
     * <p>
     * Ví dụ:
     * - CAR: 30A-999.98  -> 99998
     * - MOTORBIKE new: 29H1-123.45 -> 12345
     * - MOTORBIKE old: 29H5-9999 -> 9999
     */
    public static String extractSerialNumber(String rawInput, boolean isCar) {
        String cleanedInput = clean(rawInput);
        if (cleanedInput.isEmpty()) {
            return "";
        }

        Matcher matcher = NUMERIC_SUFFIX_PATTERN.matcher(cleanedInput);
        if (!matcher.find()) {
            return "";
        }

        String numberPart = matcher.group();

        if (isCar) {
            // Biển ô tô chuẩn hiện tại: serial 5 số
            if (numberPart.length() < 5) {
                return "";
            }
            return numberPart.substring(numberPart.length() - 5);
        }

        // Xe máy:
        // - biển mới: suffix có thể là 6 số, lấy 5 số cuối
        // - biển cũ: suffix 5 số kiểu 5 + 9999, bỏ số đầu để lấy 4 số cuối
        if (numberPart.length() > 5) {
            return numberPart.substring(numberPart.length() - 5);
        }

        if (numberPart.length() == 5) {
            return numberPart.substring(1); // 29H5-9999 -> 9999
        }

        if (numberPart.length() == 4) {
            return numberPart;
        }

        return "";
    }

    /**
     * Normalize để lưu DB theo format đẹp/canonical.
     * <p>
     * Ví dụ:
     * - 30a99998      -> 30A-999.98
     * - 30A - 999.98  -> 30A-999.98
     * - 29h112345     -> 29H1-123.45
     * - 29H5-9999     -> 29H5-9999
     */
    public static String normalizePlateNumber(String rawInput, boolean isCar) {
        String cleanedInput = clean(rawInput);
        if (cleanedInput.isEmpty()) {
            return "";
        }

        String serialNumber = extractSerialNumber(cleanedInput, isCar);
        if (serialNumber.isEmpty()) {
            return "";
        }

        String prefix = cleanedInput.substring(0, cleanedInput.length() - serialNumber.length());
        if (prefix.isEmpty()) {
            return "";
        }

        if (serialNumber.length() == 5) {
            return prefix + "-" + serialNumber.substring(0, 3) + "." + serialNumber.substring(3);
        }

        if (serialNumber.length() == 4) {
            return prefix + "-" + serialNumber;
        }

        return "";
    }

    /**
     * Thực ra method cũ của bạn đang trả về province code, không phải local symbol.
     * Ví dụ: 30A-999.98 -> 30
     */
    public static String extractProvinceCode(String rawInput) {
        String cleanedInput = clean(rawInput);
        if (cleanedInput.length() < 2) {
            return "";
        }

        String prefix = cleanedInput.substring(0, 2);
        return prefix.matches("\\d{2}") ? prefix : "";
    }
}