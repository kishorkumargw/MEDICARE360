package com.medicare360.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** A flat, rounded, coloured button that looks the same on Windows, macOS and Linux. */
public class StyledButton extends JButton {

    private final Color normal;
    private final Color hover;
    private boolean mouseOver;

    public StyledButton(String text, Color color) {
        super(text);
        this.normal = color;
        this.hover = color.darker();
        setFont(Theme.BOLD);
        setForeground(Color.WHITE);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { mouseOver = true; repaint(); }
            @Override public void mouseExited(MouseEvent e) { mouseOver = false; repaint(); }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(!isEnabled() ? new Color(0xB8C2C8) : (mouseOver ? hover : normal));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
        g2.dispose();
        super.paintComponent(g);
    }
}
