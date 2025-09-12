package com.example.tests.db;

import com.example.db.*;
import org.testng.annotations.*;
import java.sql.*;
import java.util.List;
import static org.testng.Assert.assertTrue;

public class SqlDemoTests {

    @BeforeClass
    public void setup() throws Exception {
        DbManager.init(); DbManager.initSchema();
        // Seed minimal data to make queries meaningful
        long u1 = UserDao.ensure("john.doe","john@example.com");
        long p1 = ProductDao.insert("SKU-100","Tee",12.50,"apparel");
        long p2 = ProductDao.insert("SKU-200","Jeans",39.90,"apparel");
        long p3 = ProductDao.insert("SKU-300","Mouse",19.90,"electronics");

        long o1 = OrderDao.insertOrderWithItems(u1, List.of(
                new OrderDao.OrderItemData(p1, 2, 12.50),
                new OrderDao.OrderItemData(p2, 1, 39.90),
                new OrderDao.OrderItemData(p3, 3, 19.90)
        ));
        assertTrue(o1 > 0);
    }

    @Test(description="JOIN across users/orders/items/products")
    public void joinQuery() throws Exception {
        String sql = """
            SELECT o.id, u.username, p.title, oi.qty, oi.price
            FROM orders o
            JOIN users u ON u.id=o.user_id
            JOIN order_items oi ON oi.order_id=o.id
            JOIN products p ON p.id=oi.product_id
            WHERE u.username='john.doe'
        """.replace("title","name"); // H2 uses 'name' in our schema

        try (Statement st = DbManager.get().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            assertTrue(rs.next(), "Join should return at least one row");
        }
    }

    @Test(description="Aggregate revenue by category")
    public void aggregateQuery() throws Exception {
        String sql = """
            SELECT p.category, SUM(oi.qty * oi.price) AS revenue
            FROM order_items oi
            JOIN products p ON p.id = oi.product_id
            GROUP BY p.category
        """;
        try (Statement st = DbManager.get().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            assertTrue(rs.next(), "Aggregate should return rows");
        }
    }

    @Test(description="Subquery: top 2 most sold products by qty")
    public void subqueryTopSold() throws Exception {
        String sql = """
            SELECT p.name, t.total_qty
            FROM products p
            JOIN (
              SELECT product_id, SUM(qty) AS total_qty
              FROM order_items
              GROUP BY product_id
              ORDER BY total_qty DESC
              LIMIT 2
            ) t ON t.product_id = p.id
        """;
        try (Statement st = DbManager.get().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            assertTrue(rs.next(), "Subquery should return rows");
        }
    }

    @Test(description="Index usage example (EXPLAIN ANALYZE)")
    public void indexExplain() throws Exception {
        String sql = "EXPLAIN ANALYZE SELECT * FROM products WHERE category='apparel'";
        try (Statement st = DbManager.get().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            assertTrue(rs.next(), "Explain should return a plan");
            // You can print the plan if you like:
            System.out.println(rs.getString(1));
        }
    }
}

