package vn.edu.uth.quanlidaythem.service;

import java.text.Normalizer;

/**
 * Tiện ích chuẩn hoá tên vai trò về dạng chuẩn (ADMIN/TEACHER/STUDENT).
 * Hỗ trợ các biến thể như có dấu, khoảng trắng, prefix ROLE_...
 */
public final class RoleUtils {

    private RoleUtils() {}

    public static String normalize(String input) {
        if (input == null || input.isBlank()) {
            return "STUDENT";
        }

        String noDiacritics = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        String cleaned = noDiacritics
                .toUpperCase()
                .trim()
                .replaceAll("^ROLE_", "")
                .replace('-', '_')
                .replace(' ', '_')
                .replaceAll("_+", "_");

        switch (cleaned) {
            case "ADMIN":
                return "ADMIN";
            case "TEACHER":
            case "GIAO_VIEN":
            case "GIAOVIEN":
            case "GV":
            case "GIANG_VIEN":
            case "GIANGVIEN":
                return "TEACHER";
            case "STUDENT":
            case "HOC_SINH":
            case "HOCSINH":
            case "HS":
                return "STUDENT";
            default:
                return "STUDENT";
        }
    }
}

