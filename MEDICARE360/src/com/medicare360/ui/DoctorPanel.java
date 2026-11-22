package com.medicare360.ui;

import com.medicare360.db.DoctorDAO;
import com.medicare360.model.Doctor;
import com.medicare360.util.Money;
import com.medicare360.util.ValidationException;
import com.medicare360.util.Validator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.sql.SQLException;
import java.util.List;

/** Doctor Management screen: add and view doctors. */
public class DoctorPanel extends JPanel implements Refreshable {

    private static final String[] SPECIALIZATIONS = {"General Medicine", "Cardiology", "Pediatrics", "Orthopedics",
            "Dermatology", "Neurology", "Gynecology", "ENT", "Ophthalmology", "Psychiatry", "Dentistry"};

    private final DoctorDAO dao = new DoctorDAO();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Name", "Specialization", "Phone", "Email", "Fee"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(tableModel);
    private final JLabel countLabel = Theme.mutedLabel(" ");

    private final JTextField nameField = Theme.textField();
    private final JComboBox<String> specializationBox = Theme.comboBox(SPECIALIZATIONS);
    private final JTextField phoneField = Theme.textField();
    private final JTextField emailField = Theme.textField();
    private final JTextField feeField = Theme.textField();

    public DoctorPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel titles = new JPanel(new BorderLayout());
        titles.setOpaque(false);
        titles.add(Theme.pageTitle("Doctor Management"), BorderLayout.NORTH);
        titles.add(Theme.mutedLabel("Register new doctors and view the doctor list"), BorderLayout.SOUTH);
        add(titles, BorderLayout.NORTH);

        add(buildForm(), BorderLayout.WEST);

        Theme.styleTable(table);
        int[] widths = {35, 140, 115, 95, 215, 85};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(Theme.tableScroll(table), BorderLayout.CENTER);
        center.add(countLabel, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);
    }

    private JScrollPane buildForm() {
        specializationBox.setEditable(true);   // lets the user type a specialization that is not in the list

        FormPanel form = new FormPanel("New Doctor");
        form.addField("Full name (e.g. Dr. Anil Kumar) *", nameField);
        form.addField("Specialization *", specializationBox);
        form.addField("Phone (10 digits) *", phoneField);
        form.addField("Email *", emailField);
        form.addField("Consultation fee (Rs.) *", feeField);

        JButton add = new StyledButton("Add Doctor", Theme.PRIMARY);
        JButton clear = new StyledButton("Clear Form", new java.awt.Color(0x64748B));
        form.addButtons(add, clear);
        form.addFiller();

        add.addActionListener(e -> addDoctor());
        clear.addActionListener(e -> clearForm());

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getViewport().setBackground(Theme.BACKGROUND);
        scroll.setPreferredSize(new Dimension(300, 100));
        return scroll;
    }

    @Override
    public void refresh() {
        try {
            List<Doctor> doctors = dao.findAll();
            tableModel.setRowCount(0);
            for (Doctor d : doctors) {
                tableModel.addRow(new Object[]{d.getId(), d.getName(), d.getSpecialization(), d.getPhone(),
                        d.getEmail(), Money.format(d.getConsultationFee())});
            }
            countLabel.setText(doctors.size() + " doctor(s) registered.");
        } catch (SQLException e) {
            Msg.databaseError(this, e);
        }
    }

    private void addDoctor() {
        try {
            String name = Validator.requireText(nameField.getText(), "Name");
            Object chosen = specializationBox.getEditor().getItem();
            String specialization = Validator.requireText(chosen == null ? "" : chosen.toString(), "Specialization");
            String phone = Validator.requirePhone(phoneField.getText());
            String email = Validator.requireEmail(emailField.getText());
            double fee = Validator.parseAmount(feeField.getText(), "Consultation fee", false);

            Doctor doctor = new Doctor(-1, name, phone, specialization, email, fee);
            dao.insert(doctor);
            Msg.success(this, name + " was added as doctor ID " + doctor.getId() + ".");
            clearForm();
            refresh();
        } catch (ValidationException e) {
            Msg.warning(this, e.getMessage());
        } catch (SQLException e) {
            Msg.databaseError(this, e);
        }
    }

    private void clearForm() {
        nameField.setText("");
        specializationBox.setSelectedIndex(0);
        phoneField.setText("");
        emailField.setText("");
        feeField.setText("");
    }
}
