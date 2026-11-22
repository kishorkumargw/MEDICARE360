package com.medicare360.ui;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;

/** Table cell that shows a status word (Scheduled, Paid, ...) in a matching colour. */
public class StatusCellRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                   boolean focus, int row, int column) {
        super.getTableCellRendererComponent(table, value, selected, false, row, column);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 6, 0, 6));
        if (!selected) {
            setBackground(row % 2 == 0 ? Color.WHITE : Theme.TABLE_STRIPE);
        }
        String status = value == null ? "" : value.toString();
        switch (status) {
            case "Scheduled": setForeground(Theme.INFO); break;
            case "Completed":
            case "Paid":      setForeground(Theme.SUCCESS); break;
            case "Cancelled": setForeground(Theme.DANGER); break;
            case "Pending":   setForeground(Theme.WARNING); break;
            default:          setForeground(Theme.TEXT);
        }
        setFont(Theme.BOLD);
        return this;
    }
}
