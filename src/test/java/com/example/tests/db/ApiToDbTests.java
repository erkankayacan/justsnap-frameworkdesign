package com.example.tests.db;

import com.example.db.*;
import io.restassured.http.ContentType;
import org.testng.annotations.*;
import static io.restassured.RestAssured.given;
import static org.testng.Assert.*;

public class ApiToDbTests {

    @BeforeClass public void setup() throws Exception { DbManager.init(); }

    @Test(description = "Verify data insertion after API POST")
    public void insertAfterApiPost() throws Exception {
        var body = """
            {"title":"Test Backpack","price":49.99,"description":"Test item",
             "image":"https://i.pravatar.cc","category":"test-cat"}
        """;

        var resp = given().contentType(ContentType.JSON).body(body)
                .when().post("https://fakestoreapi.com/products")
                .then().statusCode(org.hamcrest.Matchers.anyOf(
                        org.hamcrest.Matchers.is(200), org.hamcrest.Matchers.is(201)))
                .extract().jsonPath();

        String title = resp.getString("title");
        double price = resp.getDouble("price");
        String category = resp.getString("category");
        String sku = "API-" + title.toLowerCase().replaceAll("\\s+","-");

        long id = ProductDao.insert(sku, title, price, category);
        assertTrue(id > 0);
        assertEquals(ProductDao.countBySku(sku), 1);
    }
}
