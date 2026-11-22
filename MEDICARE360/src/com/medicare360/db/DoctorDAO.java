package com.medicare360.db;

import com.medicare360.model.Doctor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Database access for doctors (add and view). */
public class DoctorDAO implements Dao<Doctor> {

    @Override
    public List<Doctor> findAll() throws SQLException {
        List<Doctor> doctors = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM doctors ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                doctors.add(new Doctor(rs.getInt("id"), rs.getString("name"), rs.getString("phone"),
                        rs.getString("specialization"), rs.getString("email"),
                        rs.getDouble("consultation_fee")));
            }
        }
        return doctors;
    }

    @Override
    public void insert(Doctor d) throws SQLException {
        String sql = "INSERT INTO doctors (name, specialization, phone, email, consultation_fee) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, d.getName());
            ps.setString(2, d.getSpecialization());
            ps.setString(3, d.getPhone());
            ps.setString(4, d.getEmail());
            ps.setDouble(5, d.getConsultationFee());
            ps.executeUpdate();
            d.setId(Database.lastInsertId(connection));
        }
    }

    public int count() throws SQLException {
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM doctors");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}
