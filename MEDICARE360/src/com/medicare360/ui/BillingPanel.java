package com.medicare360.ui;

import com.medicare360.db.AppointmentDAO;
import com.medicare360.db.BillDAO;
import com.medicare360.db.PatientDAO;
import com.medicare360.model.Appointment;
import com.medicare360.model.Bill;
import com.medicare360.model.Patient;
import com.medicare360.util.Money;
import com.medicare360.util.ValidationException;
import com.medicare360.util.Validator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/** Billing screen: generate bills and view them. */
public class BillingPanel extends JPanel implements Refreshable {

    private static final String NO_APPOINTMENT = "No appointment (walk-in)";

    private final BillDAO billDao = new BillDAO();
    private final PatientDAO patientDao = new PatientDAO();
    private final AppointmentDAO appointmentDao = new AppointmentDAO();

    private final JComboBox<Patient> patientBox = new JComboBox<>();
    private final JComboBox<Object> appointmentBox = new JComboBox<>();   // holds NO_APPOINTMENT text or Appointment objects
    private final JTextField consultationField = Theme.textField();
    private final JTextField medicineField = Theme.textField();
    private final JTextField otherField = Theme.textField();
    private final JComboBox<String> statusBox = Theme.comboBox(new String[]{Bill.PAID, Bill.PENDING});
    private final JLabel totalLabel = new JLabel(Money.format(0));

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Bill #", "Date", "Patient", "Doctor Fee", "Medicine", "Other", "Total", "Status"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(tableModel);
    private final JLabel countLabel = Theme.mutedLabel(" ");

    /** The bills currently shown, in the same order as they were loaded. */
    private List<Bill> shownBills = new java.util.ArrayList<>();
    /** True while we fill the drop-downs ourselves, so their listeners do not react. */
    private boolean loading = false;

    public BillingPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel titles = new JPanel(new BorderLayout());
        titles.setOpaque(false);
        titles.add(Theme.pageTitle("Billing"), BorderLayout.NORTH);
        titles.add(Theme.mutedLabel("Generate bills for patients and view all bills"), BorderLayout.SOUTH);
        add(titles, BorderLayout.NORTH);

        add(buildForm(), BorderLayout.WEST);
        add(buildTablePanel(), BorderLayout.CENTER);
    }

    private JScrollPane buildForm() {
        Theme.styleCombo(patientBox);
        Theme.styleCombo(appointmentBox);
        totalLabel.setFont(Theme.HEADING);
        totalLabel.setForeground(Theme.PRIMARY);
        consultationField.setText("0.00");

        FormPanel form = new FormPanel("Generate Bill");
        form.addField("Patient *", patientBox);
        form.addField("Appointment (optional)", appointmentBox);
        form.addField("Consultation fee (Rs.)", consultationField);
        form.addField("Medicine charges (Rs.)", medicineField);
        form.addField("Other charges (Rs.)", otherField);
        form.addField("Payment status", statusBox);

        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setBackground(Theme.BACKGROUND);
        totalRow.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        JLabel totalText = new JLabel("Total amount");
        totalText.setFont(Theme.BOLD);
        totalRow.add(totalText, BorderLayout.WEST);
        totalRow.add(totalLabel, BorderLayout.EAST);
        form.addComponent(totalRow, 16);

        JButton generate = new StyledButton("Generate Bill", Theme.PRIMARY);
        JButton clear = new StyledButton("Clear Form", new java.awt.Color(0x64748B));
        form.addButtons(generate, clear);
        form.addFiller();

        // Live total + automatic fee from the chosen appointment
        Runnable updateTotal = this::updateTotal;
        Theme.onTextChange(consultationField, updateTotal);
        Theme.onTextChange(medicineField, updateTotal);
        Theme.onTextChange(otherField, updateTotal);
        patientBox.addActionListener(e -> {
            if (!loading) {
                loadAppointmentsForSelectedPatient();
            }
        });
        appointmentBox.addActionListener(e -> {
            if (!loading && appointmentBox.getSelectedItem() instanceof Appointment) {
                Appointment a = (Appointment) appointmentBox.getSelectedItem();
                consultationField.setText(String.format(Locale.US, "%.2f", a.getDoctorFee()));
            }
        });
        generate.addActionListener(e -> generateBill());
        clear.addActionListener(e -> clearForm());

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getViewport().setBackground(Theme.BACKGROUND);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setPreferredSize(new Dimension(300, 100));
        return scroll;
    }

    private JPanel buildTablePanel() {
        Theme.styleTable(table);
        int[] widths = {50, 90, 105, 90, 90, 85, 100, 80};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        table.getColumnModel().getColumn(7).setCellRenderer(new StatusCellRenderer());

        JButton view = new StyledButton("View Bill Details", Theme.INFO);
        JButton paid = new StyledButton("Mark as Paid", Theme.SUCCESS);
        view.addActionListener(e -> viewSelectedBill());
        paid.addActionListener(e -> markSelectedPaid());

        JPanel buttons = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(view);
        buttons.add(paid);

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);
        panel.add(buttons, BorderLayout.NORTH);
        panel.add(Theme.tableScroll(table), BorderLayout.CENTER);
        panel.add(countLabel, BorderLayout.SOUTH);
        return panel;
    }

    // ---------- Loading data ----------

    @Override
    public void refresh() {
        loadPatients();
        reloadTable();
    }

    private void loadPatients() {
        loading = true;
        try {
            Patient old = (Patient) patientBox.getSelectedItem();
            patientBox.removeAllItems();
            for (Patient p : patientDao.findAll()) {
                patientBox.addItem(p);
                if (old != null && p.getId() == old.getId()) {
                    patientBox.setSelectedItem(p);
                }
            }
        } catch (SQLException e) {
            Msg.databaseError(this, e);
        } finally {
            loading = false;
        }
        loadAppointmentsForSelectedPatient();
    }

    /** Lists the selected patient's appointments that have not been billed yet. */
    private void loadAppointmentsForSelectedPatient() {
        loading = true;
        try {
            appointmentBox.removeAllItems();
            appointmentBox.addItem(NO_APPOINTMENT);
            Patient patient = (Patient) patientBox.getSelectedItem();
            if (patient != null) {
                for (Appointment a : appointmentDao.findActiveByPatient(patient.getId())) {
                    if (!billDao.existsForAppointment(a.getId())) {
                        appointmentBox.addItem(a);
                    }
                }
            }
            appointmentBox.setSelectedIndex(0);
        } catch (SQLException e) {
            Msg.databaseError(this, e);
        } finally {
            loading = false;
        }
    }

    private void reloadTable() {
        try {
            shownBills = billDao.findAll();
            tableModel.setRowCount(0);
            for (Bill b : shownBills) {
                tableModel.addRow(new Object[]{b.getId(), b.getBillDate(), b.getPatientName(),
                        Money.format(b.getConsultationFee()), Money.format(b.getMedicineCharges()),
                        Money.format(b.getOtherCharges()), Money.format(b.getTotal()), b.getStatus()});
            }
            countLabel.setText(shownBills.size() + " bill(s). Total collected so far: " + Money.format(billDao.totalPaid()));
        } catch (SQLException e) {
            Msg.databaseError(this, e);
        }
    }

    // ---------- Button actions ----------

    private void updateTotal() {
        try {
            double total = Bill.calculateTotal(
                    Validator.parseAmount(consultationField.getText(), "Consultation fee", true),
                    Validator.parseAmount(medicineField.getText(), "Medicine charges", true),
                    Validator.parseAmount(otherField.getText(), "Other charges", true));
            totalLabel.setText(Money.format(total));
        } catch (ValidationException e) {
            totalLabel.setText("-");
        }
    }

    private void generateBill() {
        try {
            Patient patient = (Patient) patientBox.getSelectedItem();
            if (patient == null) {
                throw new ValidationException("There are no patients yet. Please add a patient first.");
            }
            double consultation = Validator.parseAmount(consultationField.getText(), "Consultation fee", true);
            double medicine = Validator.parseAmount(medicineField.getText(), "Medicine charges", true);
            double other = Validator.parseAmount(otherField.getText(), "Other charges", true);
            if (consultation + medicine + other <= 0) {
                throw new ValidationException("The bill total must be more than zero. Please enter at least one charge.");
            }

            Integer appointmentId = null;
            if (appointmentBox.getSelectedItem() instanceof Appointment) {
                appointmentId = ((Appointment) appointmentBox.getSelectedItem()).getId();
            }

            Bill bill = new Bill(-1, patient.getId(), appointmentId, patient.getName(), consultation,
                    medicine, other, (String) statusBox.getSelectedItem(), LocalDate.now().toString());
            billDao.insert(bill);

            clearForm();
            reloadTable();
            Msg.success(this, "Bill #" + bill.getId() + " was generated. Total: " + Money.format(bill.getTotal()));
            showReceipt(bill);
        } catch (ValidationException e) {
            Msg.warning(this, e.getMessage());
        } catch (SQLException e) {
            Msg.databaseError(this, e);
        }
    }

    private void clearForm() {
        consultationField.setText("0.00");
        medicineField.setText("");
        otherField.setText("");
        statusBox.setSelectedIndex(0);
        loadAppointmentsForSelectedPatient();
    }

    /** The bill object for the row selected in the table, or null (after showing a message). */
    private Bill selectedBill() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            Msg.warning(this, "Please click a bill in the table first.");
            return null;
        }
        int id = (Integer) tableModel.getValueAt(table.convertRowIndexToModel(viewRow), 0);
        for (Bill b : shownBills) {
            if (b.getId() == id) {
                return b;
            }
        }
        return null;
    }

    private void viewSelectedBill() {
        Bill bill = selectedBill();
        if (bill != null) {
            showReceipt(bill);
        }
    }

    private void markSelectedPaid() {
        Bill bill = selectedBill();
        if (bill == null) {
            return;
        }
        if (Bill.PAID.equals(bill.getStatus())) {
            Msg.info(this, "Bill #" + bill.getId() + " is already paid.");
            return;
        }
        try {
            if (billDao.markPaid(bill.getId()) == 1) {
                Msg.success(this, "Bill #" + bill.getId() + " is now marked as Paid.");
            }
            reloadTable();
        } catch (SQLException e) {
            Msg.databaseError(this, e);
        }
    }

    // ---------- Receipt window ----------

    /** Builds the plain-text receipt that is shown in the bill window. */
    static String buildReceiptText(Bill bill) {
        String line = "=".repeat(44);
        String thin = "-".repeat(44);
        StringBuilder text = new StringBuilder();
        text.append(line).append('\n');
        text.append("            MEDICARE360 HOSPITAL\n");
        text.append("               Patient Bill\n");
        text.append(line).append('\n');
        text.append(String.format("Bill No      : %d%n", bill.getId()));
        text.append(String.format("Date         : %s%n", bill.getBillDate()));
        text.append(String.format("Patient      : %s (ID %d)%n", bill.getPatientName(), bill.getPatientId()));
        text.append(String.format("Appointment  : %s%n",
                bill.getAppointmentId() == null ? "None" : "#" + bill.getAppointmentId()));
        text.append(thin).append('\n');
        text.append(String.format("%-22s %20s%n", "Consultation fee", Money.format(bill.getConsultationFee())));
        text.append(String.format("%-22s %20s%n", "Medicine charges", Money.format(bill.getMedicineCharges())));
        text.append(String.format("%-22s %20s%n", "Other charges", Money.format(bill.getOtherCharges())));
        text.append(thin).append('\n');
        text.append(String.format("%-22s %20s%n", "TOTAL", Money.format(bill.getTotal())));
        text.append(line).append('\n');
        text.append(String.format("Payment status: %s%n", bill.getStatus().toUpperCase()));
        text.append("Thank you for choosing MEDICARE360.\n");
        return text.toString();
    }

    private void showReceipt(Bill bill) {
        JTextArea area = new JTextArea(buildReceiptText(bill));
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        area.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        java.awt.Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, "Bill #" + bill.getId(), java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        JButton close = new StyledButton("Close", Theme.PRIMARY);
        close.addActionListener(e -> dialog.dispose());
        JPanel south = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER));
        south.add(close);
        dialog.add(new JScrollPane(area), BorderLayout.CENTER);
        dialog.add(south, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}
