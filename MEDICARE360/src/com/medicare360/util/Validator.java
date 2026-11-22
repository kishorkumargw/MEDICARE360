package com.medicare360.util;

import java.util.regex.Pattern;

/** Small helper methods that check form input and throw {@link ValidationException} with a friendly message. */
public final class Validator {

    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)+$");

    private Validator() { }

    /** Text must not be empty. Returns the trimmed text. */
    public static String requireText(String value, String fieldName) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " is required.");
        }
        return value.trim();
    }

    /** Text is allowed to be empty. Returns the trimmed text ("" if null). */
    public static String optionalText(String value) {
        return value == null ? "" : value.trim();
    }

    /** Phone number must contain exactly 10 digits (spaces and dashes are ignored). */
    public static String requirePhone(String value) throws ValidationException {
        String text = requireText(value, "Phone number");
        String digits = text.replace(" ", "").replace("-", "");
        if (!digits.matches("\\d{10}")) {
            throw new ValidationException("Phone number must have exactly 10 digits (for example 9876543210).");
        }
        return digits;
    }

    /** Email must look like name@example.com. */
    public static String requireEmail(String value) throws ValidationException {
        String text = requireText(value, "Email");
        if (!EMAIL.matcher(text).matches()) {
            throw new ValidationException("Please enter a valid email address (for example name@example.com).");
        }
        return text;
    }

    /** Whole number between min and max (inclusive). */
    public static int parseInt(String value, String fieldName, int min, int max) throws ValidationException {
        String text = requireText(value, fieldName);
        int number;
        try {
            number = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new ValidationException(fieldName + " must be a whole number.");
        }
        if (number < min || number > max) {
            throw new ValidationException(fieldName + " must be between " + min + " and " + max + ".");
        }
        return number;
    }

    /** Money amount (0 or more). When "emptyMeansZero" is true a blank box counts as 0. */
    public static double parseAmount(String value, String fieldName, boolean emptyMeansZero) throws ValidationException {
        String text = value == null ? "" : value.trim();
        if (text.isEmpty()) {
            if (emptyMeansZero) {
                return 0.0;
            }
            throw new ValidationException(fieldName + " is required.");
        }
        double amount;
        try {
            amount = Double.parseDouble(text);
        } catch (NumberFormatException e) {
            throw new ValidationException(fieldName + " must be a number (for example 500 or 499.50).");
        }
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount < 0) {
            throw new ValidationException(fieldName + " cannot be negative.");
        }
        if (amount > 10_000_000) {
            throw new ValidationException(fieldName + " is too large.");
        }
        return Math.round(amount * 100.0) / 100.0;
    }
}
