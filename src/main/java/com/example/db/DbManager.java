package com.example.db;

import java.sql.*;

public class DbManager {
    private static Connection conn;

    /** Open connection if needed and create schema. */
    public static void init() throws SQLException {
        if (conn != null && !conn.isClosed()) return;
        conn = DriverManager.getConnection("jdbc:h2:mem:ecomm;DB_CLOSE_DELAY=-1", "sa", "");
        initSchema();
    }

    /** Public so you can re-seed in individual tests if you want. */
    public static void initSchema() throws SQLException {
        try (Statement st = get().createStatement()) {
            st.execute("DROP ALL OBJECTS");

            st.execute("""
                CREATE TABLE products (
                  id IDENTITY PRIMARY KEY,
                  sku VARCHAR(64) UNIQUE NOT NULL,
                  name VARCHAR(255) NOT NULL,
                  price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
                  category VARCHAR(100),
                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
            """);

            st.execute("""
                CREATE TABLE users (
                  id IDENTITY PRIMARY KEY,
                  username VARCHAR(64) UNIQUE NOT NULL,
                  email VARCHAR(255) UNIQUE NOT NULL,
                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
            """);

            st.execute("""
                CREATE TABLE orders (
                  id IDENTITY PRIMARY KEY,
                  user_id BIGINT NOT NULL,
                  total DECIMAL(10,2) NOT NULL CHECK (total >= 0),
                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                );
            """);

            st.execute("""
                CREATE TABLE order_items (
                  order_id BIGINT NOT NULL,
                  product_id BIGINT NOT NULL,
                  qty INT NOT NULL CHECK (qty > 0),
                  price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
                  PRIMARY KEY (order_id, product_id),
                  FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
                  FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT
                );
            """);

            // Perf indexes
            st.execute("CREATE INDEX idx_products_category ON products(category)");
            st.execute("CREATE INDEX idx_order_items_product ON order_items(product_id)");
        }
    }

    public static Connection get() throws SQLException {
        if (conn == null || conn.isClosed()) init();
        return conn;
    }

    public static void close() {
        try { if (conn != null && !conn.isClosed()) conn.close(); } catch (Exception ignore) {}
        conn = null;
    }
}
