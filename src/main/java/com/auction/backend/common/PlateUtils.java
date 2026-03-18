package com.auction.backend.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlateUtils {
    public static String extractSerialNumber(String rawInput, boolean isCar) {
        if (rawInput == null || rawInput.isBlank()) return "";
        // BƯỚC 1: CLEAN DATA
        // Xóa hết dấu chấm, phẩy, gạch ngang, khoảng trắng...
        // Chỉ giữ lại Chữ (A-Z) và Số (0-9). Chuyển về chữ hoa.
        // VD: "30a - 123.45" -> "30A12345
        if (rawInput.contains("-") || rawInput.contains(".") || rawInput.contains(" ")) {
            // Tách bằng bất kỳ ký tự nào không phải chữ/số
            String[] parts = rawInput.split("[^a-zA-Z0-9]+");
            if (parts.length > 1) {
                String lastPart = parts[parts.length - 1];
                // Kiểm tra lại nếu phần cuối toàn là số thì trả về luôn
                if (lastPart.matches("\\d+")) {
                    return lastPart;
                }
            }
        }
        String cleanedInput = rawInput.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        // BƯỚC 2: Tìm chuỗi số nằm ở cuối cùng (Numeric Suffix)
        // Regex "\\d+$" nghĩa là tìm các chữ số nằm ở cuối chuỗi
        Matcher matcher = Pattern.compile("\\d+$").matcher(cleanedInput);
        if (matcher.find()) {
            String numberPart = matcher.group();
            if (isCar) {
                if (numberPart.length() > 5) {
                    return numberPart.substring(0, 5);
                }
                return numberPart;
            } else {
                if (numberPart.length() > 5) {
                    return numberPart.substring(numberPart.length() - 5);
                }
                // Nếu là biển 4 số (Tổng chuỗi số = 5, VD: 5 + 9999)
                // Đây chính là case 29H5-9999 -> clean thành 29H59999 -> suffix 59999
                if (numberPart.length() == 5) {
                    return numberPart.substring(1); // Bỏ số '5' đầu, lấy "9999"
                }
                // Các trường hợp còn lại (VD nhập thiếu, biển cũ quá) -> giữ nguyên
                return numberPart;
            }
        }
        return "";
    }

    /**
     * Tách mã tỉnh (Province Code)
     * Input: "30A-12345" -> Output: "30"
     */
    public static String extractLocalSymbol(String rawInput) {
        if (rawInput == null || rawInput.isBlank()) return "";
        String cleanedInput = rawInput.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        if (cleanedInput.length() >= 2) {
            String prefix = cleanedInput.substring(0, 2);
            // Kiểm tra xem 2 ký tự đầu có phải là số không (tránh trường hợp user nhập tào lao)
            if (prefix.matches("\\d+")) {
                return prefix;
            }
        }
        return "";
    }
}
