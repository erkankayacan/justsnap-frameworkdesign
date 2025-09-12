package com.example.db;

import java.sql.*;
import java.util.List;

public class OrderDao {

    public static long insertOrder(long userId, double total) throws SQLException {
        String sql = "INSERT INTO orders(user_id,total) VALUES(?,?)";
        try (PreparedStatement ps = DbManager.get().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, userId);
            ps.setDouble(2, total);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getLong(1) : -1;
            }
        }
    }

    public static void addItem(long orderId, long productId, int qty, double price) throws SQLException {
        String sql = "INSERT INTO order_items(order_id,product_id,qty,price) VALUES(?,?,?,?)";
        try (PreparedStatement ps = DbManager.get().prepareStatement(sql)) {
            ps.setLong(1, orderId);
            ps.setLong(2, productId);
            ps.setInt(3, qty);
            ps.setDouble(4, price);
            ps.executeUpdate();
        }
    }

    public static int countOrderItems(long orderId) throws SQLException {
        String q = "SELECT COUNT(*) FROM order_items WHERE order_id=?";
        try (PreparedStatement ps = DbManager.get().prepareStatement(q)) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1); }
        }
    }

    public static double calcOrderTotalFromItems(long orderId) throws SQLException {
        String q = "SELECT COALESCE(SUM(qty*price),0) FROM order_items WHERE order_id=?";
        try (PreparedStatement ps = DbManager.get().prepareStatement(q)) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getDouble(1); }
        }
    }

    public record OrderItemData(long productId, int qty, double price) {}

    /** Transactionally create order + items, storing computed total. */
    public static long insertOrderWithItems(long userId, List<OrderItemData> items) throws SQLException {
        Connection c = DbManager.get();
        boolean prev = c.getAutoCommit();
        c.setAutoCommit(false);
        Savepoint sp = c.setSavepoint();
        try {
            long orderId = insertOrder(userId, 0.0);
            double total = 0.0;
            for (OrderItemData it : items) {
                addItem(orderId, it.productId(), it.qty(), it.price());
                total += it.qty() * it.price();
            }
            try (PreparedStatement ps = c.prepareStatement("UPDATE orders SET total=? WHERE id=?")) {
                ps.setDouble(1, total);
                ps.setLong(2, orderId);
                ps.executeUpdate();
            }
            c.commit();
            return orderId;
        } catch (SQLException e) {
            c.rollback(sp);
            throw e;
        } finally {
            c.setAutoCommit(prev);
        }
    }
}
