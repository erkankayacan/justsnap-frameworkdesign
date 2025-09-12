package com.example.tests.db;

import com.example.db.DbManager;
import org.testng.annotations.*;
import java.sql.*;

import static org.testng.Assert.assertTrue;

public class BulkPerfTests {

    @BeforeClass public void setup() throws Exception { DbManager.init(); DbManager.initSchema(); }

    @Test(description="Batch insert performance")
    public void bulkInsertPerf() throws Exception {
        int N = 2000;
        String sql = "INSERT INTO products(sku,name,price,category) VALUES(?,?,?,?)";
        long start = System.currentTimeMillis();
        try (PreparedStatement ps = DbManager.get().prepareStatement(sql)) {
            for (int i = 0; i < N; i++) {
                ps.setString(1, "BULK-" + i);
                ps.setString(2, "Item " + i);
                ps.setDouble(3, 9.99 + (i % 5));
                ps.setString(4, i % 2 == 0 ? "catA" : "catB");
                ps.addBatch();
                if (i % 500 == 0) ps.executeBatch();
            }
            ps.executeBatch();
        }
        long ms = System.currentTimeMillis() - start;
        System.out.println("Inserted " + N + " rows in " + ms + " ms");
        assertTrue(ms < 5000, "Bulk insert should complete within 5s (adjust threshold for your CI)");
    }
}

