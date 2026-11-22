package com.medicare360.ui;

import com.medicare360.db.PatientDAO;
import com.medicare360.model.Patient;
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

/** Patient Management screen: add, view, search, update and delete patients. */
public class PatientPanel extends JPanel implements Refreshable {

    private static final String[] GENDERS = {"Male", "Female", "Other"};
    private static final String[] BLOOD_GROUPS = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};

    private final PatientDAO dao = new PatientDAO();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Name", "Age", "Gender", "Blood", "Phone", "Condition / Notes"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(tableModel);

    private final JTextField searchField = Theme.textField();
    private final JLabel resultLabel = Theme.mutedLabel(" ");

    private final JLabel selectedLabel = new JLabel("New patient");
    private final JTextField nameField = Theme.textField();
    private final JTextField ageField = Theme.textField();
    private final JComboBox<String> genderBox = Theme.comboBox(GENDERS);
    private final JComboBox<String> bloodBox = Theme.comboBox(BLOOD_GROUPS);
    private final JTextField phoneField = Theme.textField();
    private final JTextField addressField = Theme.textField();
    private final JTextField notesField = Theme.textField();

    /** ID of the patient loaded in the form, or -1 when the form is empty. */
    private int selectedId = -1;
    /** Last search text; the table is reloaded with it after every change. */
    private String activeSearch = "";

    public PatientPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildForm(), BorderLayout.WEST);
        add(buildTable(), BorderLayout.CENTER);
    }

    // ---------- Screen layout ----------

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setOpaque(false);
        JPanel titles = new JPanel(new BorderLayout());
        titles.setOpaque(false);
        titles.add(Theme.pageTitle("Patient Management"), BorderLayout.NORTH);
        titles.add(Theme.mutedLabel("Add, search, update and delete patient records"), BorderLayout.SOUTH);
        bar.add(titles, BorderLayout.WEST);

        JPanel search = new JPanel(new BorderLayout(8, 0));
        search.setOpaque(false);
        searchField.setToolTipText("Search by name, phone, blood group or ID");
        searchField.setPreferredSize(new Dimension(260, 38));
        JButton searchButton = new StyledButton("Search", Theme.PRIMARY);
        JButton showAll = new StyledButton("Show All", Theme.INFO);
        JPanel buttons = new JPanel(new java.awt.GridLayout(1, 2, 8, 0));
        buttons.setOpaque(false);
        buttons.add(searchButton);
        buttons.add(showAll);
        search.add(searchField, BorderLayout.CENTER);
        search.add(buttons, BorderLayout.EAST);
        JPanel searchWrap = new JPanel(new java.awt.GridBagLayout());
        searchWrap.setOpaque(false);
        searchWrap.add(search);
        bar.add(searchWrap, BorderLayout.EAST);

        searchButton.addActionListener(e -> doSearch());
        searchField.addActionListener(e -> doSearch());
        showAll.addActionListener(e -> {
            searchField.setText("");
            activeSearch = "";
            reloadTable();
        });
        return bar;
    }

    private JScrollPane buildForm() {
        FormPanel form = new FormPanel("Patient Details");
        selectedLabel.setFont(Theme.SMALL);
        selectedLabel.setForeground(Theme.MUTED);
        form.addComponent(selectedLabel, 0);
        form.addField("Full name *", nameField);
        form.addField("Age *", ageField);
        form.addField("Gender", genderBox);
        form.addField("Blood group", bloodBox);
        form.addField("Phone (10 digits) *", phoneField);
        form.addField("Address", addressField);
        form.addField("Condition / notes", notesField);

        JButton add = new StyledButton("Add Patient", Theme.PRIMARY);
        JButton update = new StyledButton("Update", Theme.INFO);
        JButton delete = new StyledButton("Delete", Theme.DANGER);
        JButton clear = new StyledButton("Clear Form", new java.awt.Color(0x64748B));
        form.addButtons(add, update, delete, clear);
        form.addFiller();

        add.addActionListener(e -> addPatient());
        update.addActionListener(e -> updatePatient());
        delete.addActionListener(e -> deletePatient());
        clear.addActionListener(e -> clearForm());

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getViewport().setBackground(Theme.BACKGROUND);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setPreferredSize(new Dimension(310, 100));
        return scroll;
    }

    private JPanel buildTable() {
        Theme.styleTable(table);
        int[] widths = {40, 140, 45, 70, 62, 105, 190};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                loadSelectedPatientIntoForm();
            }
        });

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);
        panel.add(Theme.tableScroll(table), BorderLayout.CENTER);
        panel.add(resultLabel, BorderLayout.SOUTH);
        return panel;
    }

    // ---------- Loading data ----------

    @Override
    public void refresh() {
        reloadTable();
    }

    private void reloadTable() {
        try {
            List<Patient> patients = activeSearch.isEmpty() ? dao.findAll() : dao.search(activeSearch);
            tableModel.setRowCount(0);
            for (Patient p : patients) {
                tableModel.addRow(new Object[]{p.getId(), p.getName(), p.getAge(), p.getGender(),
                        p.getBloodGroup(), p.getPhone(), p.getNotes()});
            }
            if (activeSearch.isEmpty()) {
                resultLabel.setText("Showing all " + patients.size() + " patient(s). Click a row to edit it.");
            } else if (patients.isEmpty()) {
                resultLabel.setText("No patients found for \"" + activeSearch + "\".");
            } else {
                resultLabel.setText(patients.size() + " patient(s) found for \"" + activeSearch + "\".");
            }
        } catch (SQLException e) {
            Msg.databaseError(this, e);
        }
    }

    private void doSearch() {
        activeSearch = searchField.getText().trim();
        reloadTable();
    }

    private void loadSelectedPatientIntoForm() {
        int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
        int id = (Integer) tableModel.getValueAt(modelRow, 0);
        try {
            Patient p = dao.findById(id);
            if (p == null) {
                return;
            }
            selectedId = p.getId();
            selectedLabel.setText("Editing patient #" + p.getId());
            nameField.setText(p.getName());
            ageField.setText(String.valueOf(p.getAge()));
            genderBox.setSelectedItem(p.getGender());
            bloodBox.setSelectedItem(p.getBloodGroup());
            phoneField.setText(p.getPhone());
            addressField.setText(p.getAddress());
            notesField.setText(p.getNotes());
        } catch (SQLException e) {
            Msg.databaseError(this, e);
        }
    }

    // ---------- Button actions ----------

    /** Reads and checks the form. The id is -1 for a new patient. */
    private Patient readForm(int id) throws ValidationException {
        String name = Validator.requireText(nameField.getText(), "Name");
        int age = Validator.parseInt(ageField.getText(), "Age", 0, 120);
        String phone = Validator.requirePhone(phoneField.getText());
        return new Patient(id, name, phone, age, (String) genderBox.getSelectedItem(),
                (String) bloodBox.getSelectedItem(), Validator.optionalText(addressField.getText()),
                Validator.optionalText(notesField.getText()), null);
    }

    private void addPatient() {
        try {
            Patient patient = readForm(-1);
            dao.insert(patient);
            Msg.success(this, "Patient \"" + patient.getName() + "\" was added with ID " + patient.getId() + ".");
            clearForm();
            reloadTable();
        } catch (ValidationException e) {
            Msg.warning(this, e.getMessage());
        } catch (SQLException e) {
            Msg.databaseError(this, e);
        }
    }

    private void updatePatient() {
        if (selectedId < 0) {
            Msg.warning(this, "Please click a patient in the table first, then change the details and press Update.");
            return;
        }
        try {
            Patient patient = readForm(selectedId);
            if (dao.update(patient) == 1) {
                Msg.success(this, "Patient #" + selectedId + " was updated.");
                reloadTable();
            } else {
                Msg.error(this, "This patient no longer exists.");
                clearForm();
                reloadTable();
            }
        } catch (ValidationException e) {
            Msg.warning(this, e.getMessage());
        } catch (SQLException e) {
            Msg.databaseError(this, e);
        }
    }

    private void deletePatient() {
        if (selectedId < 0) {
            Msg.warning(this, "Please click the patient you want to delete in the table first.");
            return;
        }
        String name = nameField.getText().trim();
        boolean sure = Msg.confirm(this, "Delete patient #" + selectedId + " (" + name + ")?\n\n"
                + "Their appointments and bills will also be deleted.\nThis cannot be undone.");
        if (!sure) {
            return;
        }
        try {
            if (dao.delete(selectedId) == 1) {
                Msg.success(this, "Patient \"" + name + "\" was deleted.");
            } else {
                Msg.error(this, "This patient no longer exists.");
            }
            clearForm();
            reloadTable();
        } catch (SQLException e) {
            Msg.databaseError(this, e);
        }
    }

    private void clearForm() {
        selectedId = -1;
        selectedLabel.setText("New patient");
        nameField.setText("");
        ageField.setText("");
        genderBox.setSelectedIndex(0);
        bloodBox.setSelectedIndex(0);
        phoneField.setText("");
        addressField.setText("");
        notesField.setText("");
        table.clearSelection();
    }
}
