package com.example.api.clients;

import com.example.api.RequestSpecs;
import com.example.utils.Config;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class UserClient {
    public Response getAllUsers()          { return given().spec(RequestSpecs.json("/users")).when().get().then().extract().response(); }
    public Response getUser(int id)        { return given().spec(RequestSpecs.json("/users")).when().get("/"+id).then().extract().response(); }
    public Response createUser(Object body){ return given().spec(RequestSpecs.json("/users")).body(body).when().post().then().extract().response(); }
    public Response updateUser(int id, Object body){ return given().spec(RequestSpecs.json("/users")).body(body).when().put("/"+id).then().extract().response(); }
    public Response deleteUser(int id)     { return given().spec(RequestSpecs.json("/users")).when().delete("/"+id).then().extract().response(); }

    /** fakestore: POST /auth/login -> { token } */
    public Response authenticate(String username, String password) {
        var payload = Map.of("username", username, "password", password);
        return given().spec(RequestSpecs.json("/auth")).body(payload).when().post("/login").then().extract().response();
    }

    @BeforeClass
    public void setupApi() {
        // Base URI from config (with fallback)
        RestAssured.baseURI = Config.apiBaseUrl();

        // Set base path to /users for all requests
        RestAssured.basePath = "/users";

        // Default request spec: JSON content-type + accept
        RestAssured.requestSpecification = new RequestSpecBuilder()
                .setBaseUri(RestAssured.baseURI)
                .setBasePath(RestAssured.basePath)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .setRelaxedHTTPSValidation()
                .build();

        // Log requests/responses when validation fails
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
}
