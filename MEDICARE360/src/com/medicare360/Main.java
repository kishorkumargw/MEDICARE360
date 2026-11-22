package com.medicare360;

import com.medicare360.db.Database;
import com.medicare360.ui.LoginFrame;
import com.medicare360.ui.Theme;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Font;
import java.sql.SQLException;

/** Start of the application: prepares the database and opens the login window. */
public class Main {

    public static void main(String[] args) {
        // Same look on every operating system, so our colours are always respected.
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
            // If this fails Java simply uses its default look - nothing to do.
        }
        Font font = Theme.BASE;
        UIManager.put("OptionPane.messageFont", font);
        UIManager.put("OptionPane.buttonFont", font);
        UIManager.put("ToolTip.font", font);

        try {
            Database.initialize();      // creates data/medicare360.db and the demo data on first run
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "MEDICARE360 could not start.\n\n" + e.getMessage(),
                    "MEDICARE360 - Startup error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return;
        }

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
