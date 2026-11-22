package com.medicare360.ui;

import com.medicare360.db.AppointmentDAO;
import com.medicare360.db.BillDAO;
import com.medicare360.db.DoctorDAO;
import com.medicare360.db.PatientDAO;
import com.medicare360.model.Appointment;
import com.medicare360.util.Money;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Dashboard: patient, doctor and appointment counts plus the next appointments. */
public class DashboardPanel extends JPanel implements Refreshable {

    private final PatientDAO patientDao = new PatientDAO();
    private final DoctorDAO doctorDao = new DoctorDAO();
    private final AppointmentDAO appointmentDao = new AppointmentDAO();
    private final BillDAO billDao = new BillDAO();

    private final JLabel patientsValue = bigNumber();
    private final JLabel doctorsValue = bigNumber();
    private final JLabel appointmentsValue = bigNumber();
    private final JLabel revenueValue = bigNumber();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Date", "Time", "Patient", "Doctor", "Reason"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(tableModel);

    public DashboardPanel(String adminName) {
        setLayout(new BorderLayout(0, 20));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel titles = new JPanel(new BorderLayout());
        titles.setOpaque(false);
        titles.add(Theme.pageTitle("Dashboard"), BorderLayout.NORTH);
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy"));
        titles.add(Theme.mutedLabel("Welcome, " + adminName + "  |  " + today), BorderLayout.SOUTH);

        JPanel cards = new JPanel(new GridLayout(1, 4, 16, 0));
        cards.setOpaque(false);
        cards.add(statCard("Total Patients", patientsValue, Theme.PRIMARY));
        cards.add(statCard("Total Doctors", doctorsValue, Theme.INFO));
        cards.add(statCard("Scheduled Appointments", appointmentsValue, new Color(0x7C3AED)));
        cards.add(statCard("Revenue Collected", revenueValue, Theme.SUCCESS));

        JPanel top = new JPanel(new BorderLayout(0, 16));
        top.setOpaque(false);
        top.add(titles, BorderLayout.NORTH);
        top.add(cards, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        Theme.styleTable(table);
        JLabel heading = new JLabel("Upcoming Appointments");
        heading.setFont(Theme.HEADING);
        heading.setForeground(Theme.SIDEBAR);
        JPanel lower = new JPanel(new BorderLayout(0, 8));
        lower.setOpaque(false);
        lower.add(heading, BorderLayout.NORTH);
        lower.add(Theme.tableScroll(table), BorderLayout.CENTER);
        add(lower, BorderLayout.CENTER);
    }

    private static JLabel bigNumber() {
        JLabel label = new JLabel("0");
        label.setFont(Theme.BIG_NUMBER);
        label.setForeground(Theme.TEXT);
        return label;
    }

    /** A white card with a coloured stripe on the left, a caption and a big number. */
    private static JPanel statCard(String caption, JLabel value, Color stripe) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Theme.CARD);
        card.setBorder(BorderFactory.createCompoundBorder(Theme.lineBorder(),
                BorderFactory.createMatteBorder(0, 6, 0, 0, stripe)));
        JPanel inner = new JPanel(new BorderLayout(0, 4));
        inner.setOpaque(false);
        inner.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        inner.add(Theme.mutedLabel(caption), BorderLayout.NORTH);
        inner.add(value, BorderLayout.CENTER);
        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    @Override
    public void refresh() {
        try {
            patientsValue.setText(String.valueOf(patientDao.count()));
            doctorsValue.setText(String.valueOf(doctorDao.count()));
            appointmentsValue.setText(String.valueOf(appointmentDao.countScheduled()));
            revenueValue.setText(Money.format(billDao.totalPaid()));

            List<Appointment> upcoming = appointmentDao.findUpcoming(8);
            tableModel.setRowCount(0);
            for (Appointment a : upcoming) {
                tableModel.addRow(new Object[]{a.getDate(), a.getTime(), a.getPatientName(),
                        a.getDoctorName(), a.getReason()});
            }
        } catch (SQLException e) {
            Msg.databaseError(this, e);
        }
    }
}
