package com.ecommerce.util;

import com.ecommerce.exception.SecurityValidationException;

public class InputSanitizer {

    /**
     * Sanitize and validate user input
     */
    public static String sanitizeString(String input, int maxLength) {
        if (input == null) {
            throw new SecurityValidationException("Input cannot be null");
        }

        if (input.length() > maxLength) {
            throw new SecurityValidationException("Input exceeds maximum length of " + maxLength);
        }

        // Check for SQL injection
        if (ValidationUtil.containsSQLInjection(input)) {
            throw new SecurityValidationException("Invalid input detected - potential SQL injection");
        }

        // Sanitize special characters
        return ValidationUtil.sanitizeInput(input);
    }

    /**
     * Validate and sanitize email
     */
    public static String validateAndSanitizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new SecurityValidationException("Email cannot be empty");
        }

        String trimmedEmail = email.trim().toLowerCase();

        if (!ValidationUtil.isValidEmail(trimmedEmail)) {
            throw new SecurityValidationException("Invalid email format");
        }

        if (trimmedEmail.length() > 255) {
            throw new SecurityValidationException("Email exceeds maximum length");
        }

        return trimmedEmail;
    }

    /**
     * Validate and sanitize password
     */
    public static String validateAndSanitizePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new SecurityValidationException("Password cannot be empty");
        }

        if (!ValidationUtil.isStrongPassword(password)) {
            throw new SecurityValidationException(
                    "Password must be at least 8 characters and contain uppercase, lowercase, number, and special character"
            );
        }

        return password;
    }

    /**
     * Validate and sanitize name
     */
    public static String validateAndSanitizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new SecurityValidationException("Name cannot be empty");
        }

        String trimmedName = name.trim();

        if (!ValidationUtil.isValidName(trimmedName)) {
            throw new SecurityValidationException("Name contains invalid characters");
        }

        if (trimmedName.length() > 50) {
            throw new SecurityValidationException("Name exceeds maximum length");
        }

        return trimmedName;
    }

    /**
     * Validate and sanitize phone
     */
    public static String validateAndSanitizePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new SecurityValidationException("Phone cannot be empty");
        }

        String sanitizedPhone = phone.replaceAll("[^0-9+]", "");

        if (!ValidationUtil.isValidPhone(sanitizedPhone)) {
            throw new SecurityValidationException("Invalid phone format");
        }

        return sanitizedPhone;
    }

    /**
     * Validate and sanitize URL
     */
    public static String validateAndSanitizeUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new SecurityValidationException("URL cannot be empty");
        }

        String trimmedUrl = url.trim();

        if (!ValidationUtil.isValidUrl(trimmedUrl)) {
            throw new SecurityValidationException("Invalid URL format");
        }

        return trimmedUrl;
    }
}
