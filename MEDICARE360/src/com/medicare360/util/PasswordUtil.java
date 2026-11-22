package com.medicare360.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Passwords are never stored as plain text. We store a SHA-256 hash instead. */
public final class PasswordUtil {

    private PasswordUtil() { }

    public static String hash(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 exists in every Java installation, so this should never happen.
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
