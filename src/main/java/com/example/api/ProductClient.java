package com.example.api;

import com.example.api.RequestSpecs;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class ProductClient {
    public Response getAll()              { return given().spec(RequestSpecs.json("/products")).when().get().then().extract().response(); }
    public Response getById(int id)       { return given().spec(RequestSpecs.json("/products")).when().get("/"+id).then().extract().response(); }
    public Response create(Object body)   { return given().spec(RequestSpecs.json("/products")).body(body).when().post().then().extract().response(); }
    public Response update(int id, Object body){ return given().spec(RequestSpecs.json("/products")).body(body).when().put("/"+id).then().extract().response(); }
    public Response patch(int id, Object body) { return given().spec(RequestSpecs.json("/products")).body(body).when().patch("/"+id).then().extract().response(); }
    public Response delete(int id)        { return given().spec(RequestSpecs.json("/products")).when().delete("/"+id).then().extract().response(); }
}
