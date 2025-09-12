package com.example.api;

import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class ProductClient extends ApiBase {

    public Response getAll(){
        return given().spec(spec).when().get("/products");
    }
    public Response getById(int id){
        return given().spec(spec).when().get("/products/{id}", id);
    }
    public Response create(String body){
        return given().spec(spec).body(body).when().post("/products");
    }
    public Response update(int id, String body){
        return given().spec(spec).body(body).when().put("/products/{id}", id);
    }
    public Response delete(int id){
        return given().spec(spec).when().delete("/products/{id}", id);
    }
}
