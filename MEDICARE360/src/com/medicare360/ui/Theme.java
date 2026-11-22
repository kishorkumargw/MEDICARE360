package com.medicare360.ui;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.text.JTextComponent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;

/** One place for all colours, fonts and small styling helpers so every screen looks the same. */
public final class Theme {

    // Colours (hospital teal / blue palette)
    public static final Color PRIMARY = new Color(0x0F766E);
    public static final Color SIDEBAR = new Color(0x0B3C49);
    public static final Color SIDEBAR_HOVER = new Color(0x15586B);
    public static final Color BACKGROUND = new Color(0xF2F7F9);
    public static final Color CARD = Color.WHITE;
    public static final Color TEXT = new Color(0x1F2937);
    public static final Color MUTED = new Color(0x6B7280);
    public static final Color BORDER = new Color(0xD5DEE3);
    public static final Color DANGER = new Color(0xDC2626);
    public static final Color SUCCESS = new Color(0x15803D);
    public static final Color INFO = new Color(0x2563EB);
    public static final Color WARNING = new Color(0xB45309);
    public static final Color TABLE_STRIPE = new Color(0xF7FBFC);
    public static final Color TABLE_SELECTION = new Color(0xCFEFEA);

    // Fonts
    private static final String FAMILY = pickFontFamily();
    public static final Font BASE = new Font(FAMILY, Font.PLAIN, 14);
    public static final Font BOLD = new Font(FAMILY, Font.BOLD, 14);
    public static final Font SMALL = new Font(FAMILY, Font.PLAIN, 12);
    public static final Font TITLE = new Font(FAMILY, Font.BOLD, 24);
    public static final Font HEADING = new Font(FAMILY, Font.BOLD, 16);
    public static final Font BIG_NUMBER = new Font(FAMILY, Font.BOLD, 30);

    private Theme() { }

    private static String pickFontFamily() {
        String[] available = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String wanted : new String[]{"Segoe UI", "SF Pro Text", "Helvetica Neue", "Ubuntu", "Noto Sans"}) {
            if (Arrays.asList(available).contains(wanted)) {
                return wanted;
            }
        }
        return Font.SANS_SERIF;
    }

    public static Border lineBorder() {
        return BorderFactory.createLineBorder(BORDER);
    }

    /** White card look: thin border and padding. */
    public static Border cardBorder(int padding) {
        return BorderFactory.createCompoundBorder(lineBorder(),
                BorderFactory.createEmptyBorder(padding, padding, padding, padding));
    }

    public static JTextField textField() {
        JTextField field = new JTextField();
        field.setFont(BASE);
        field.setForeground(TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(lineBorder(), BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        return field;
    }

    public static <T> JComboBox<T> comboBox(T[] items) {
        JComboBox<T> box = new JComboBox<>(items);
        styleCombo(box);
        return box;
    }

    public static void styleCombo(JComboBox<?> box) {
        box.setFont(BASE);
        box.setBackground(Color.WHITE);
        box.setForeground(TEXT);
    }

    public static JLabel pageTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(TITLE);
        label.setForeground(SIDEBAR);
        return label;
    }

    public static JLabel mutedLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(BASE);
        label.setForeground(MUTED);
        return label;
    }

    /** Applies the standard table look: striped rows, tall rows, coloured header. */
    public static void styleTable(JTable table) {
        table.setFont(BASE);
        table.setForeground(TEXT);
        table.setRowHeight(30);
        table.setGridColor(new Color(0xE5ECEF));
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new java.awt.Dimension(0, 1));
        table.setSelectionBackground(TABLE_SELECTION);
        table.setSelectionForeground(TEXT);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);

        DefaultTableCellRenderer cell = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean selected,
                                                           boolean focus, int row, int column) {
                super.getTableCellRendererComponent(t, value, selected, false, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
                if (!selected) {
                    setBackground(row % 2 == 0 ? Color.WHITE : TABLE_STRIPE);
                }
                return this;
            }
        };
        table.setDefaultRenderer(Object.class, cell);

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new java.awt.Dimension(10, 36));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean selected,
                                                           boolean focus, int row, int column) {
                super.getTableCellRendererComponent(t, value, false, false, row, column);
                setBackground(SIDEBAR);
                setForeground(Color.WHITE);
                setFont(BOLD);
                setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
                return this;
            }
        });
    }

    public static JScrollPane tableScroll(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setColumnHeaderView(table.getTableHeader());
        scroll.setBorder(lineBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    /** Runs "action" every time the text of a text box changes. */
    public static void onTextChange(JTextComponent component, Runnable action) {
        component.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { action.run(); }
            @Override public void removeUpdate(DocumentEvent e) { action.run(); }
            @Override public void changedUpdate(DocumentEvent e) { action.run(); }
        });
    }

    /** Draws the hospital logo (white rounded square with a teal cross) as an image. */
    public static BufferedImage logo(int size, Color background, Color cross) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(background);
        g.fillRoundRect(0, 0, size, size, size / 4, size / 4);
        g.setColor(cross);
        int arm = size / 5;
        int mid = size / 2;
        int length = size * 3 / 5;
        g.fillRoundRect(mid - arm / 2, mid - length / 2, arm, length, arm / 2, arm / 2);
        g.fillRoundRect(mid - length / 2, mid - arm / 2, length, arm, arm / 2, arm / 2);
        g.dispose();
        return image;
    }
}
