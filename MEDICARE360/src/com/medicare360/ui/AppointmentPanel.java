package com.medicare360.ui;

import com.medicare360.db.AppointmentDAO;
import com.medicare360.db.DoctorDAO;
import com.medicare360.db.PatientDAO;
import com.medicare360.model.Appointment;
import com.medicare360.model.Doctor;
import com.medicare360.model.Patient;
import com.medicare360.util.ValidationException;
import com.medicare360.util.Validator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/** Appointment Management screen: book, view and cancel appointments. */
public class AppointmentPanel extends JPanel implements Refreshable {

    private static final String[] FILTERS = {"All", "Scheduled", "Completed", "Cancelled"};

    private final AppointmentDAO appointmentDao = new AppointmentDAO();
    private final PatientDAO patientDao = new PatientDAO();
    private final DoctorDAO doctorDao = new DoctorDAO();

    private final JComboBox<Patient> patientBox = new JComboBox<>();
    private final JComboBox<Doctor> doctorBox = new JComboBox<>();
    private final JSpinner dateSpinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
    private final JComboBox<String> timeBox = Theme.comboBox(buildTimeSlots());
    private final JTextField reasonField = Theme.textField();

    private final JComboBox<String> filterBox = Theme.comboBox(FILTERS);
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Date", "Time", "Patient", "Doctor", "Reason", "Status"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(tableModel);
    private final JLabel countLabel = Theme.mutedLabel(" ");

    public AppointmentPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel titles = new JPanel(new BorderLayout());
        titles.setOpaque(false);
        titles.add(Theme.pageTitle("Appointment Management"), BorderLayout.NORTH);
        titles.add(Theme.mutedLabel("Book new appointments and cancel existing ones"), BorderLayout.SOUTH);
        add(titles, BorderLayout.NORTH);

        add(buildForm(), BorderLayout.WEST);
        add(buildTablePanel(), BorderLayout.CENTER);
    }

    /** 09:00, 09:30, ... 16:30 */
    private static String[] buildTimeSlots() {
        List<String> slots = new ArrayList<>();
        for (LocalTime t = LocalTime.of(9, 0); t.isBefore(LocalTime.of(17, 0)); t = t.plusMinutes(30)) {
            slots.add(t.toString());   // LocalTime prints as HH:mm
        }
        return slots.toArray(new String[0]);
    }

    private JScrollPane buildForm() {
        Theme.styleCombo(patientBox);
        Theme.styleCombo(doctorBox);
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        dateSpinner.setFont(Theme.BASE);

        FormPanel form = new FormPanel("Book Appointment");
        form.addField("Patient *", patientBox);
        form.addField("Doctor *", doctorBox);
        form.addField("Date (use the arrows or type) *", dateSpinner);
        form.addField("Time *", timeBox);
        form.addField("Reason for visit *", reasonField);

        JButton book = new StyledButton("Book Appointment", Theme.PRIMARY);
        JPanel buttonWrap = new JPanel(new BorderLayout());
        buttonWrap.setOpaque(false);
        buttonWrap.add(book, BorderLayout.CENTER);
        form.addComponent(buttonWrap, 18);
        form.addFiller();

        book.addActionListener(e -> bookAppointment());

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getViewport().setBackground(Theme.BACKGROUND);
        scroll.setPreferredSize(new Dimension(300, 100));
        return scroll;
    }

    private JPanel buildTablePanel() {
        Theme.styleTable(table);
        int[] widths = {35, 95, 55, 120, 130, 135, 90};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        table.getColumnModel().getColumn(6).setCellRenderer(new StatusCellRenderer());

        filterBox.addActionListener(e -> reloadTable());
        JButton cancel = new StyledButton("Cancel Selected Appointment", Theme.DANGER);
        cancel.addActionListener(e -> cancelSelected());

        JPanel top = new JPanel(new BorderLayout(8, 0));
        top.setOpaque(false);
        JPanel filter = new JPanel(new BorderLayout(8, 0));
        filter.setOpaque(false);
        filter.add(Theme.mutedLabel("Show:"), BorderLayout.WEST);
        filter.add(filterBox, BorderLayout.CENTER);
        top.add(filter, BorderLayout.WEST);
        top.add(cancel, BorderLayout.EAST);

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);
        panel.add(top, BorderLayout.NORTH);
        panel.add(Theme.tableScroll(table), BorderLayout.CENTER);
        panel.add(countLabel, BorderLayout.SOUTH);
        return panel;
    }

    // ---------- Loading data ----------

    @Override
    public void refresh() {
        loadPeople();
        reloadTable();
    }

    /** Fills the patient and doctor drop-downs from the database (keeps the current choice if possible). */
    private void loadPeople() {
        try {
            Patient oldPatient = (Patient) patientBox.getSelectedItem();
            Doctor oldDoctor = (Doctor) doctorBox.getSelectedItem();

            patientBox.removeAllItems();
            for (Patient p : patientDao.findAll()) {
                patientBox.addItem(p);
                if (oldPatient != null && p.getId() == oldPatient.getId()) {
                    patientBox.setSelectedItem(p);
                }
            }
            doctorBox.removeAllItems();
            for (Doctor d : doctorDao.findAll()) {
                doctorBox.addItem(d);
                if (oldDoctor != null && d.getId() == oldDoctor.getId()) {
                    doctorBox.setSelectedItem(d);
                }
            }
        } catch (SQLException e) {
            Msg.databaseError(this, e);
        }
    }

    private void reloadTable() {
        try {
            String filter = (String) filterBox.getSelectedItem();
            List<Appointment> list = "All".equals(filter)
                    ? appointmentDao.findAll() : appointmentDao.findByStatus(filter);
            tableModel.setRowCount(0);
            for (Appointment a : list) {
                tableModel.addRow(new Object[]{a.getId(), a.getDate(), a.getTime(), a.getPatientName(),
                        a.getDoctorName(), a.getReason(), a.getStatus()});
            }
            countLabel.setText(list.size() + " appointment(s) shown.");
        } catch (SQLException e) {
            Msg.databaseError(this, e);
        }
    }

    // ---------- Button actions ----------

    private void bookAppointment() {
        try {
            Patient patient = (Patient) patientBox.getSelectedItem();
            Doctor doctor = (Doctor) doctorBox.getSelectedItem();
            if (patient == null) {
                throw new ValidationException("There are no patients yet. Please add a patient first.");
            }
            if (doctor == null) {
                throw new ValidationException("There are no doctors yet. Please add a doctor first.");
            }

            LocalDate date = ((Date) dateSpinner.getValue()).toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
            String time = (String) timeBox.getSelectedItem();
            if (date.isBefore(LocalDate.now())) {
                throw new ValidationException("The appointment date cannot be in the past.");
            }
            if (date.equals(LocalDate.now()) && LocalTime.parse(time).isBefore(LocalTime.now())) {
                throw new ValidationException("That time has already passed today. Please choose a later time.");
            }
            String reason = Validator.requireText(reasonField.getText(), "Reason for visit");

            if (appointmentDao.isSlotTaken(doctor.getId(), date.toString(), time)) {
                throw new ValidationException(doctor.getName() + " is already booked on " + date
                        + " at " + time + ".\nPlease choose another time.");
            }

            Appointment appointment = new Appointment(-1, patient.getId(), doctor.getId(), patient.getName(),
                    doctor.getName(), doctor.getConsultationFee(), date.toString(), time, reason, Appointment.SCHEDULED);
            appointmentDao.insert(appointment);

            Msg.success(this, "Appointment #" + appointment.getId() + " booked for " + patient.getName()
                    + " with " + doctor.getName() + "\non " + date + " at " + time + ".");
            reasonField.setText("");
            reloadTable();
        } catch (ValidationException e) {
            Msg.warning(this, e.getMessage());
        } catch (SQLException e) {
            Msg.databaseError(this, e);
        }
    }

    private void cancelSelected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            Msg.warning(this, "Please click the appointment you want to cancel in the table first.");
            return;
        }
        int row = table.convertRowIndexToModel(viewRow);
        int id = (Integer) tableModel.getValueAt(row, 0);
        String status = (String) tableModel.getValueAt(row, 6);
        if (!Appointment.SCHEDULED.equals(status)) {
            Msg.warning(this, "Only scheduled appointments can be cancelled. This one is already " + status.toLowerCase() + ".");
            return;
        }
        String who = tableModel.getValueAt(row, 3) + " on " + tableModel.getValueAt(row, 1)
                + " at " + tableModel.getValueAt(row, 2);
        if (!Msg.confirm(this, "Cancel appointment #" + id + " for " + who + "?")) {
            return;
        }
        try {
            if (appointmentDao.cancel(id) == 1) {
                Msg.success(this, "Appointment #" + id + " was cancelled.");
            } else {
                Msg.error(this, "The appointment could not be cancelled because it is no longer scheduled.");
            }
            reloadTable();
        } catch (SQLException e) {
            Msg.databaseError(this, e);
        }
    }
}
