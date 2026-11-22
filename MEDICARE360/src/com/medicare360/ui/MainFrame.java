package com.medicare360.ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;

/** The main window: a sidebar on the left and the selected screen on the right. */
public class MainFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);
    private final Map<String, Refreshable> screens = new LinkedHashMap<>();
    private final Map<String, NavButton> navButtons = new LinkedHashMap<>();

    public MainFrame(String adminName) {
        super("MEDICARE360 - Hospital Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setIconImage(Theme.logo(64, Theme.PRIMARY, Color.WHITE));
        setMinimumSize(new Dimension(1100, 680));
        setSize(1280, 760);
        setLocationRelativeTo(null);

        content.setBackground(Theme.BACKGROUND);
        addScreen("Dashboard", new DashboardPanel(adminName));
        addScreen("Patients", new PatientPanel());
        addScreen("Doctors", new DoctorPanel());
        addScreen("Appointments", new AppointmentPanel());
        addScreen("Billing", new BillingPanel());

        add(buildSidebar(adminName), BorderLayout.WEST);
        add(content, BorderLayout.CENTER);

        showScreen("Dashboard");
    }

    /** Registers a screen. Every screen must be a JPanel that also implements Refreshable. */
    private <T extends JPanel & Refreshable> void addScreen(String name, T panel) {
        content.add(panel, name);
        screens.put(name, panel);
    }

    private void showScreen(String name) {
        screens.get(name).refresh();            // always show fresh data from the database
        cardLayout.show(content, name);
        for (Map.Entry<String, NavButton> entry : navButtons.entrySet()) {
            entry.getValue().setActive(entry.getKey().equals(name));
        }
    }

    private JPanel buildSidebar(String adminName) {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Theme.SIDEBAR);
        sidebar.setPreferredSize(new Dimension(230, 100));
        sidebar.setBorder(BorderFactory.createEmptyBorder(24, 14, 20, 14));

        JLabel logo = new JLabel(new ImageIcon(Theme.logo(44, Color.WHITE, Theme.PRIMARY)));
        logo.setAlignmentX(LEFT_ALIGNMENT);
        JLabel brand = new JLabel("MEDICARE360");
        brand.setFont(Theme.HEADING.deriveFont(20f));
        brand.setForeground(Color.WHITE);
        brand.setAlignmentX(LEFT_ALIGNMENT);
        JLabel tagline = new JLabel("Hospital Management");
        tagline.setFont(Theme.SMALL);
        tagline.setForeground(new Color(0x9FC3CF));
        tagline.setAlignmentX(LEFT_ALIGNMENT);

        sidebar.add(logo);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(brand);
        sidebar.add(tagline);
        sidebar.add(Box.createVerticalStrut(30));

        for (String name : screens.keySet()) {
            NavButton button = new NavButton(name);
            button.addActionListener(e -> showScreen(name));
            navButtons.put(name, button);
            sidebar.add(button);
            sidebar.add(Box.createVerticalStrut(6));
        }

        sidebar.add(Box.createVerticalGlue());

        JLabel user = new JLabel("Signed in as " + adminName);
        user.setFont(Theme.SMALL);
        user.setForeground(new Color(0x9FC3CF));
        user.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(user);
        sidebar.add(Box.createVerticalStrut(10));

        JButton logout = new StyledButton("Logout", Theme.DANGER);
        logout.setAlignmentX(LEFT_ALIGNMENT);
        logout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        logout.addActionListener(e -> logout());
        sidebar.add(logout);
        return sidebar;
    }

    private void logout() {
        if (Msg.confirm(this, "Do you want to log out?")) {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        }
    }

    /** A sidebar menu button that highlights itself when its screen is open. */
    private static class NavButton extends JButton {
        private boolean active;
        private boolean hover;

        NavButton(String text) {
            super(text);
            setFont(Theme.BOLD);
            setForeground(Color.WHITE);
            setHorizontalAlignment(LEFT);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(11, 16, 11, 16));
            setAlignmentX(LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }

        void setActive(boolean active) {
            this.active = active;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (active) {
                g2.setColor(Theme.PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            } else if (hover) {
                g2.setColor(Theme.SIDEBAR_HOVER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
