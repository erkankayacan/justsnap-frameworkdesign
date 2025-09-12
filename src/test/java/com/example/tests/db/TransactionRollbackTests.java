package com.example.tests.db;

import com.example.db.*;
import org.testng.annotations.*;
import java.sql.*;

import static org.testng.Assert.assertEquals;

public class TransactionRollbackTests {

    @BeforeClass public void setup() throws Exception { DbManager.init(); DbManager.initSchema(); }

    @Test(description = "Rollback if second insert fails")
    public void rollbackOnFailure() throws Exception {
        Connection c = DbManager.get();
        c.setAutoCommit(false);
        Savepoint sp = c.setSavepoint();
        try {
            ProductDao.insert("TX-OK", "Good", 5.0, "tx");
            // Duplicate SKU to force failure
            ProductDao.insert("TX-OK", "Dup", 7.0, "tx");
            c.commit();
        } catch (SQLException e) {
            c.rollback(sp);
        } finally {
            c.setAutoCommit(true);
        }
        assertEquals(ProductDao.countBySku("TX-OK"), 0, "Rollback should remove partial inserts");
    }
}
