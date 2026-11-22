package com.medicare360.db;

import com.medicare360.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Database access for administrator logins. */
public class AdminDAO {

    /** Returns the admin's full name when the login is correct, or null when it is wrong. */
    public String login(String username, String password) throws SQLException {
        String sql = "SELECT full_name FROM admins WHERE username = ? AND password_hash = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, PasswordUtil.hash(password));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("full_name") : null;
            }
        }
    }
}
