package com.medicare360.ui;

import javax.swing.JOptionPane;
import java.awt.Component;
import java.sql.SQLException;

/** Short helpers for the pop-up messages shown to the user. */
public final class Msg {

    private static final String TITLE = "MEDICARE360";

    private Msg() { }

    public static void success(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, TITLE + " - Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, TITLE, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, TITLE + " - Error", JOptionPane.ERROR_MESSAGE);
    }

    /** Wrong form input: tells the user what to fix. */
    public static void warning(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, TITLE + " - Please check", JOptionPane.WARNING_MESSAGE);
    }

    public static void databaseError(Component parent, SQLException e) {
        error(parent, "Something went wrong with the database.\n\n" + e.getMessage());
    }

    /** Asks a Yes/No question. Returns true when the user clicks Yes. */
    public static boolean confirm(Component parent, String question) {
        int choice = JOptionPane.showConfirmDialog(parent, question, TITLE + " - Please confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return choice == JOptionPane.YES_OPTION;
    }
}
