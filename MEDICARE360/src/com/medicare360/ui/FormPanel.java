package com.medicare360.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

/** A white card that stacks "label above field" rows. Used for every data-entry form. */
public class FormPanel extends JPanel {

    private int row = 0;

    public FormPanel(String heading) {
        super(new GridBagLayout());
        setBackground(Theme.CARD);
        setBorder(Theme.cardBorder(16));

        JLabel title = new JLabel(heading);
        title.setFont(Theme.HEADING);
        title.setForeground(Theme.PRIMARY);
        add(title, constraints(new Insets(0, 0, 4, 0)));
    }

    private GridBagConstraints constraints(Insets insets) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = row++;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = insets;
        return c;
    }

    /** Adds a small label with the input component underneath. */
    public void addField(String labelText, JComponent field) {
        JLabel label = new JLabel(labelText);
        label.setFont(Theme.SMALL);
        label.setForeground(Theme.MUTED);
        add(label, constraints(new Insets(10, 0, 3, 0)));
        // Fields stretch to the card width; a small preferred width stops long drop-down items from widening the card.
        field.setPreferredSize(new java.awt.Dimension(150, field.getPreferredSize().height));
        add(field, constraints(new Insets(0, 0, 0, 0)));
    }

    /** Adds any component (for example a read-only total) across the full width. */
    public void addComponent(JComponent component, int topGap) {
        add(component, constraints(new Insets(topGap, 0, 0, 0)));
    }

    /** Adds buttons in a grid with two buttons per row. */
    public void addButtons(JButton... buttons) {
        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder());
        for (JButton button : buttons) {
            panel.add(button);
        }
        add(panel, constraints(new Insets(18, 0, 0, 0)));
    }

    /** Pushes everything above it to the top of the card. Call once after the last row. */
    public void addFiller() {
        GridBagConstraints c = constraints(new Insets(0, 0, 0, 0));
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        add(new JPanel() {{ setOpaque(false); }}, c);
    }
}
