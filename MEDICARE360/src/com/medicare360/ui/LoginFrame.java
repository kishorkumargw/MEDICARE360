package com.medicare360.ui;

import com.medicare360.db.AdminDAO;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.sql.SQLException;

/** The first screen: administrator login. */
public class LoginFrame extends JFrame {

    private final AdminDAO adminDao = new AdminDAO();
    private final JTextField usernameField = Theme.textField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JLabel messageLabel = new JLabel(" ");

    public LoginFrame() {
        super("MEDICARE360 - Admin Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setIconImage(Theme.logo(64, Theme.PRIMARY, Color.WHITE));
        setSize(860, 500);
        setResizable(false);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new GridLayout(1, 2));
        root.add(buildBrandPanel());
        root.add(buildLoginPanel());
        add(root, BorderLayout.CENTER);

        getRootPane().setDefaultButton(loginButton);   // pressing Enter clicks Login
    }

    private JButton loginButton;

    private JPanel buildBrandPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Theme.PRIMARY);
        panel.setBorder(BorderFactory.createEmptyBorder(80, 44, 40, 30));

        JLabel logo = new JLabel(new ImageIcon(Theme.logo(84, Color.WHITE, Theme.PRIMARY)));
        JLabel name = new JLabel("MEDICARE360");
        name.setFont(Theme.TITLE.deriveFont(32f));
        name.setForeground(Color.WHITE);
        JLabel tagline = new JLabel("Hospital Management System");
        tagline.setFont(Theme.HEADING);
        tagline.setForeground(new Color(0xCDEFEA));
        JLabel line = new JLabel("<html>Patients, doctors, appointments and<br>billing - all in one simple place.</html>");
        line.setFont(Theme.BASE);
        line.setForeground(new Color(0xCDEFEA));

        for (JLabel label : new JLabel[]{logo, name, tagline, line}) {
            label.setAlignmentX(LEFT_ALIGNMENT);
        }
        panel.add(logo);
        panel.add(Box.createVerticalStrut(22));
        panel.add(name);
        panel.add(Box.createVerticalStrut(4));
        panel.add(tagline);
        panel.add(Box.createVerticalStrut(24));
        panel.add(line);
        return panel;
    }

    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(70, 50, 30, 50));

        JLabel title = new JLabel("Admin Login");
        title.setFont(Theme.TITLE);
        title.setForeground(Theme.SIDEBAR);
        JLabel sub = Theme.mutedLabel("Sign in to continue");

        JLabel userLabel = fieldLabel("Username");
        JLabel passLabel = fieldLabel("Password");
        passwordField.setFont(Theme.BASE);
        passwordField.setBorder(usernameField.getBorder());

        messageLabel.setFont(Theme.SMALL);
        messageLabel.setForeground(Theme.DANGER);

        loginButton = new StyledButton("Login", Theme.PRIMARY);
        loginButton.addActionListener(e -> doLogin());
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        JLabel hint = Theme.mutedLabel("Demo login:  admin  /  admin123");
        hint.setFont(Theme.SMALL);

        for (javax.swing.JComponent c : new javax.swing.JComponent[]{title, sub, userLabel, usernameField,
                passLabel, passwordField, messageLabel, loginButton, hint}) {
            c.setAlignmentX(LEFT_ALIGNMENT);
        }
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        panel.add(title);
        panel.add(Box.createVerticalStrut(4));
        panel.add(sub);
        panel.add(Box.createVerticalStrut(24));
        panel.add(userLabel);
        panel.add(Box.createVerticalStrut(4));
        panel.add(usernameField);
        panel.add(Box.createVerticalStrut(14));
        panel.add(passLabel);
        panel.add(Box.createVerticalStrut(4));
        panel.add(passwordField);
        panel.add(Box.createVerticalStrut(8));
        panel.add(messageLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(loginButton);
        panel.add(Box.createVerticalStrut(14));
        panel.add(hint);
        return panel;
    }

    private static JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.SMALL);
        label.setForeground(Theme.MUTED);
        return label;
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter both username and password.");
            return;
        }
        try {
            String adminName = adminDao.login(username, password);
            if (adminName == null) {
                messageLabel.setText("Wrong username or password. Please try again.");
                passwordField.setText("");
                passwordField.requestFocusInWindow();
                return;
            }
            dispose();
            new MainFrame(adminName).setVisible(true);
        } catch (SQLException e) {
            Msg.databaseError(this, e);
        }
    }
}
