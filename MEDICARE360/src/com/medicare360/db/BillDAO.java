package com.medicare360.db;

import com.medicare360.model.Bill;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/** Database access for bills (generate and view). */
public class BillDAO implements Dao<Bill> {

    @Override
    public List<Bill> findAll() throws SQLException {
        String sql = "SELECT b.id, b.patient_id, b.appointment_id, p.name AS patient_name, "
                + "b.consultation_fee, b.medicine_charges, b.other_charges, b.status, b.bill_date "
                + "FROM bills b JOIN patients p ON p.id = b.patient_id ORDER BY b.id DESC";
        List<Bill> bills = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int appointmentId = rs.getInt("appointment_id");
                Integer linkedAppointment = rs.wasNull() ? null : appointmentId;   // NULL in database -> null in Java
                bills.add(new Bill(rs.getInt("id"), rs.getInt("patient_id"), linkedAppointment,
                        rs.getString("patient_name"), rs.getDouble("consultation_fee"),
                        rs.getDouble("medicine_charges"), rs.getDouble("other_charges"),
                        rs.getString("status"), rs.getString("bill_date")));
            }
        }
        return bills;
    }

    @Override
    public void insert(Bill b) throws SQLException {
        String sql = "INSERT INTO bills (patient_id, appointment_id, consultation_fee, medicine_charges, "
                + "other_charges, total, status, bill_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, b.getPatientId());
            if (b.getAppointmentId() == null) {
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(2, b.getAppointmentId());
            }
            ps.setDouble(3, b.getConsultationFee());
            ps.setDouble(4, b.getMedicineCharges());
            ps.setDouble(5, b.getOtherCharges());
            ps.setDouble(6, b.getTotal());
            ps.setString(7, b.getStatus());
            ps.setString(8, b.getBillDate());
            ps.executeUpdate();
            b.setId(Database.lastInsertId(connection));
        }
    }

    /** Changes a pending bill to Paid. Returns 1 on success, 0 if it was not pending. */
    public int markPaid(int billId) throws SQLException {
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "UPDATE bills SET status = 'Paid' WHERE id = ? AND status = 'Pending'")) {
            ps.setInt(1, billId);
            return ps.executeUpdate();
        }
    }

    /** True when a bill already exists for this appointment (prevents billing twice). */
    public boolean existsForAppointment(int appointmentId) throws SQLException {
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT COUNT(*) FROM bills WHERE appointment_id = ?")) {
            ps.setInt(1, appointmentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /** Total money collected from bills that are marked Paid (used by the dashboard). */
    public double totalPaid() throws SQLException {
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT COALESCE(SUM(total), 0) FROM bills WHERE status = 'Paid'");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0.0;
        }
    }
}
