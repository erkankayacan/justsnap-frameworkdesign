
package com.example.tests.db;

import com.example.db.*;
import org.testng.annotations.*;
import java.sql.SQLException;
import static org.testng.Assert.*;

public class DatabaseIntegrityTests {

    @BeforeClass public void setup() throws Exception { DbManager.init(); DbManager.initSchema(); }

    @Test(description="Unique SKU should fail", expectedExceptions = SQLException.class)
    public void integrityConstraints_uniqueSku() throws Exception {
        ProductDao.insert("SKU-001", "A", 10.0, "cat");
        ProductDao.insert("SKU-001", "B", 20.0, "cat2"); // duplicate -> SQLException
    }

    @Test(description="Price check constraint fails", expectedExceptions = SQLException.class)
    public void integrityConstraints_priceCheck() throws Exception {
        ProductDao.insert("SKU-NEG", "Bad", -1.0, "cat"); // price >= 0 enforced
    }
}
