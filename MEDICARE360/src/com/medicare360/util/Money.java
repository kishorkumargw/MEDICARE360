package com.medicare360.util;

import java.util.Locale;

/** Formats money amounts the same way everywhere in the application. */
public final class Money {

    private Money() { }

    /** 1150 becomes "Rs. 1,150.00". */
    public static String format(double amount) {
        return String.format(Locale.US, "Rs. %,.2f", amount);
    }
}
