package com.medicare360.util;

/** Thrown when something typed into a form is missing or invalid. The message is shown to the user. */
public class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }
}
