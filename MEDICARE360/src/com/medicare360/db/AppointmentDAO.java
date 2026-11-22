package com.medicare360.db;

import com.medicare360.model.Appointment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Database access for appointments (book, view, cancel). */
public class AppointmentDAO implements Dao<Appointment> {

    /** Joins the patient and doctor tables so we can show names instead of ID numbers. */
    private static final String SELECT_WITH_NAMES =
            "SELECT a.id, a.patient_id, a.doctor_id, p.name AS patient_name, d.name AS doctor_name, "
          + "d.consultation_fee AS doctor_fee, a.appt_date, a.appt_time, a.reason, a.status "
          + "FROM appointments a "
          + "JOIN patients p ON p.id = a.patient_id "
          + "JOIN doctors d ON d.id = a.doctor_id ";

    private Appointment map(ResultSet rs) throws SQLException {
        return new Appointment(rs.getInt("id"), rs.getInt("patient_id"), rs.getInt("doctor_id"),
                rs.getString("patient_name"), rs.getString("doctor_name"), rs.getDouble("doctor_fee"),
                rs.getString("appt_date"), rs.getString("appt_time"),
                rs.getString("reason"), rs.getString("status"));
    }

    @Override
    public List<Appointment> findAll() throws SQLException {
        return query(SELECT_WITH_NAMES + "ORDER BY a.appt_date, a.appt_time", null);
    }

    /** Appointments with one status, e.g. "Scheduled". */
    public List<Appointment> findByStatus(String status) throws SQLException {
        return query(SELECT_WITH_NAMES + "WHERE a.status = ? ORDER BY a.appt_date, a.appt_time", status);
    }

    /** The next few scheduled appointments starting from today (used by the dashboard). */
    public List<Appointment> findUpcoming(int limit) throws SQLException {
        String sql = SELECT_WITH_NAMES
                + "WHERE a.status = 'Scheduled' AND a.appt_date >= ? "
                + "ORDER BY a.appt_date, a.appt_time LIMIT " + limit;
        return query(sql, LocalDate.now().toString());
    }

    /** Appointments of one patient that are not cancelled (used when creating a bill). */
    public List<Appointment> findActiveByPatient(int patientId) throws SQLException {
        String sql = SELECT_WITH_NAMES
                + "WHERE a.patient_id = ? AND a.status <> 'Cancelled' ORDER BY a.appt_date DESC, a.appt_time DESC";
        List<Appointment> list = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    /** Runs a query that has at most one text parameter. */
    private List<Appointment> query(String sql, String parameter) throws SQLException {
        List<Appointment> list = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            if (parameter != null) {
                ps.setString(1, parameter);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    /** True when this doctor already has a scheduled appointment at that date and time. */
    public boolean isSlotTaken(int doctorId, String date, String time) throws SQLException {
        String sql = "SELECT COUNT(*) FROM appointments "
                + "WHERE doctor_id = ? AND appt_date = ? AND appt_time = ? AND status = 'Scheduled'";
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ps.setString(2, date);
            ps.setString(3, time);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    @Override
    public void insert(Appointment a) throws SQLException {
        String sql = "INSERT INTO appointments (patient_id, doctor_id, appt_date, appt_time, reason, status) "
                + "VALUES (?, ?, ?, ?, ?, 'Scheduled')";
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, a.getPatientId());
            ps.setInt(2, a.getDoctorId());
            ps.setString(3, a.getDate());
            ps.setString(4, a.getTime());
            ps.setString(5, a.getReason());
            ps.executeUpdate();
            a.setId(Database.lastInsertId(connection));
        }
    }

    /** Cancels a scheduled appointment. Returns 1 if it was cancelled, 0 if it was not scheduled. */
    public int cancel(int appointmentId) throws SQLException {
        String sql = "UPDATE appointments SET status = 'Cancelled' WHERE id = ? AND status = 'Scheduled'";
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, appointmentId);
            return ps.executeUpdate();
        }
    }

    /** Number of appointments that are currently scheduled (used by the dashboard). */
    public int countScheduled() throws SQLException {
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT COUNT(*) FROM appointments WHERE status = 'Scheduled'");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}
