package com.ecommerce.util;

import java.util.regex.Pattern;

public class ValidationUtil {

    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"
    );

    // Phone validation pattern (10-15 digits)
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[+]?[0-9]{10,15}$"
    );

    // Strong password pattern (min 8 chars, uppercase, lowercase, number, special char)
    private static final Pattern STRONG_PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );

    // SQL injection patterns
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
            "(.*)(--|;|'|\"|(\\*)|or|and|drop|delete|insert|update|select|union|exec|execute)(.*)",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validate phone number format
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Validate password strength
     * Requirements: min 8 chars, uppercase, lowercase, number, special character
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.isBlank()) {
            return false;
        }
        return STRONG_PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Validate password confirmation
     */
    public static boolean passwordsMatch(String password, String confirmPassword) {
        if (password == null || confirmPassword == null) {
            return false;
        }
        return password.equals(confirmPassword);
    }

    /**
     * Check for potential SQL injection
     */
    public static boolean containsSQLInjection(String input) {
        if (input == null || input.isBlank()) {
            return false;
        }
        return SQL_INJECTION_PATTERN.matcher(input).matches();
    }

    /**
     * Sanitize input by removing potentially harmful characters
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        return input
                .replaceAll("[<>\"']", "")
                .trim();
    }

    /**
     * Validate string length
     */
    public static boolean isValidLength(String input, int minLength, int maxLength) {
        if (input == null) {
            return false;
        }
        int length = input.length();
        return length >= minLength && length <= maxLength;
    }

    /**
     * Validate name format (letters and spaces only)
     */
    public static boolean isValidName(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }
        return name.matches("^[a-zA-Z\\s'-]{2,50}$");
    }

    /**
     * Validate price format
     */
    public static boolean isValidPrice(String price) {
        if (price == null || price.isBlank()) {
            return false;
        }
        try {
            double value = Double.parseDouble(price);
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validate stock quantity
     */
    public static boolean isValidStock(Integer stock) {
        return stock != null && stock >= 0;
    }

    /**
     * Validate URL format
     */
    public static boolean isValidUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        try {
            new java.net.URL(url);
            return true;
        } catch (java.net.MalformedURLException e) {
            return false;
        }
    }
}
