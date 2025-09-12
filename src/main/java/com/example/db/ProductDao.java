package com.example.db;

import java.sql.*;

public class ProductDao {

    /** Insert and return generated numeric ID. */
    public static long insert(String sku, String name, double price, String category) throws SQLException {
        String sql = "INSERT INTO products(sku,name,price,category) VALUES(?,?,?,?)";
        try (PreparedStatement ps = DbManager.get().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, sku);
            ps.setString(2, name);
            ps.setDouble(3, price);
            ps.setString(4, category);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        throw new SQLException("Insert failed: no generated key");
    }

    public static Product findBySku(String sku) throws SQLException {
        String q = "SELECT id, sku, name, price, category FROM products WHERE sku=?";
        try (PreparedStatement ps = DbManager.get().prepareStatement(q)) {
            ps.setString(1, sku);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Product(
                            rs.getLong("id"),
                            rs.getString("sku"),
                            rs.getString("name"),
                            rs.getDouble("price"),
                            rs.getString("category")
                    );
                }
            }
        }
        return null;
    }

    public static int countBySku(String sku) throws SQLException {
        String q = "SELECT COUNT(*) FROM products WHERE sku=?";
        try (PreparedStatement ps = DbManager.get().prepareStatement(q)) {
            ps.setString(1, sku);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public static void deleteAll() throws SQLException {
        try (Statement st = DbManager.get().createStatement()) { st.execute("DELETE FROM products"); }
    }
}
