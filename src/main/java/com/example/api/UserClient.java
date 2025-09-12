package com.example.api;

import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class UserClient extends ApiBase {
    public Response authenticate(String username, String password){
        String body = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
        return given().spec(spec).body(body).post("/auth/login");
    }
    public Response getUser(int id){
        return given().spec(spec).get("/users/{id}", id);
    }
    public Response create(String body){
        return given().spec(spec).body(body).post("/users");
    }
    public Response update(int id, String body){
        return given().spec(spec).body(body).put("/users/{id}", id);
    }
}
