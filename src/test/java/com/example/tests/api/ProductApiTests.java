package com.example.tests.api;

import com.example.api.ProductClient;
import io.restassured.http.ContentType;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ProductApiTests {
    private ProductClient client;



    @Test(description="GET all products")
    public void getAllProducts(){
        client.getAll().then().statusCode(200).time(lessThan(2000L));
    }

    @Test(description="GET single product by ID")
    public void getProductById(){
        client.getById(1).then().statusCode(200).body("id", equalTo(1));
    }

    @Test(description="POST new product")
    public void postNewProduct(){
        String body = "{\"title\":\"Test Jacket\",\"price\":99.9,\"description\":\"desc\",\"image\":\"url\",\"category\":\"men's clothing\"}";
        Response r = client.create(body);
        r.then().statusCode(anyOf(is(200), is(201)));
    }

    @Test(description="PUT update existing product")
    public void updateProduct(){
        String body = "{\"title\":\"Updated\",\"price\":10.5}";
        client.update(1, body).then().statusCode(anyOf(is(200), is(201)));
    }
@BeforeClass
public void init() {
    client = new ProductClient();   // <-- initialize the same field
}

    @Test(description="DELETE product")
    public void deleteProduct(){
        client.delete(1).then().statusCode(anyOf(is(200), is(201)));
    }

    @Test(description="Verify response schema for single product")
    public void schemaValidation(){
        client.getById(1).then().assertThat().body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/product-schema.json"));
    }

    @Test(description="Validate response time under 2 seconds")
    public void responseTime(){
        client.getAll().then().time(lessThan(2000L));
    }
}
