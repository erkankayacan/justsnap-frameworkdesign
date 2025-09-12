package com.example.db;

import java.sql.*;

public class UserDao {

    public static long insert(String username, String email) throws SQLException {
        String sql = "INSERT INTO users(username,email) VALUES(?,?)";
        try (PreparedStatement ps = DbManager.get().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, email);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getLong(1) : -1;
            }
        }
    }

    public static Long findIdByUsername(String username) throws SQLException {
        String q = "SELECT id FROM users WHERE username=?";
        try (PreparedStatement ps = DbManager.get().prepareStatement(q)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getLong(1) : null; }
        }
    }

    public static long ensure(String username, String email) throws SQLException {
        Long id = findIdByUsername(username);
        return (id != null) ? id : insert(username, email);
    }
}
