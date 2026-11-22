package com.medicare360.db;

import com.medicare360.model.Patient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Database access for patients (add, view, search, update, delete). */
public class PatientDAO implements Dao<Patient> {

    private Patient map(ResultSet rs) throws SQLException {
        return new Patient(rs.getInt("id"), rs.getString("name"), rs.getString("phone"),
                rs.getInt("age"), rs.getString("gender"), rs.getString("blood_group"),
                rs.getString("address"), rs.getString("notes"), rs.getString("registered_date"));
    }

    @Override
    public List<Patient> findAll() throws SQLException {
        List<Patient> patients = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM patients ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                patients.add(map(rs));
            }
        }
        return patients;
    }

    /** Finds patients whose name, phone, blood group or ID matches the keyword. */
    public List<Patient> search(String keyword) throws SQLException {
        String sql = "SELECT * FROM patients "
                + "WHERE name LIKE ? OR phone LIKE ? OR blood_group LIKE ? OR CAST(id AS TEXT) = ? "
                + "ORDER BY id";
        List<Patient> patients = new ArrayList<>();
        String like = "%" + keyword.trim() + "%";
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, keyword.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    patients.add(map(rs));
                }
            }
        }
        return patients;
    }

    /** Returns the patient with this id, or null if there is none. */
    public Patient findById(int id) throws SQLException {
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM patients WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    @Override
    public void insert(Patient p) throws SQLException {
        String sql = "INSERT INTO patients (name, age, gender, blood_group, phone, address, notes, registered_date) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setInt(2, p.getAge());
            ps.setString(3, p.getGender());
            ps.setString(4, p.getBloodGroup());
            ps.setString(5, p.getPhone());
            ps.setString(6, p.getAddress());
            ps.setString(7, p.getNotes());
            ps.setString(8, LocalDate.now().toString());
            ps.executeUpdate();
            p.setId(Database.lastInsertId(connection));
        }
    }

    /** Saves changes to an existing patient. Returns the number of rows changed (1 = success). */
    public int update(Patient p) throws SQLException {
        String sql = "UPDATE patients SET name = ?, age = ?, gender = ?, blood_group = ?, phone = ?, "
                + "address = ?, notes = ? WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setInt(2, p.getAge());
            ps.setString(3, p.getGender());
            ps.setString(4, p.getBloodGroup());
            ps.setString(5, p.getPhone());
            ps.setString(6, p.getAddress());
            ps.setString(7, p.getNotes());
            ps.setInt(8, p.getId());
            return ps.executeUpdate();
        }
    }

    /** Deletes a patient (their appointments and bills are removed automatically). */
    public int delete(int id) throws SQLException {
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement("DELETE FROM patients WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }

    public int count() throws SQLException {
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM patients");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}
