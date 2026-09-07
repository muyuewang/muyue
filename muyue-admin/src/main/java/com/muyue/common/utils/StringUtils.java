package com.muyue.common.utils;

/**
 * 字符串工具类（轻量版，避免重复造轮子时可直接使用 hutool）
 *
 * @author muyue
 */
public class StringUtils {

    public static boolean hasText(String str) {
        return str != null && !str.isBlank();
    }

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static boolean isBlank(String str) {
        return str == null || str.isBlank();
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    public static String defaultString(String str, String defaultValue) {
        return isBlank(str) ? defaultValue : str;
    }

    public static boolean matches(String str, java.util.List<String> strs) {
        if (isBlank(str) || strs == null || strs.isEmpty()) {
            return false;
        }
        for (String s : strs) {
            if (s != null && str.equals(s.trim())) {
                return true;
            }
        }
        return false;
    }
}
