package com.example.api;

import com.example.utils.Config;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;

public abstract class ApiBase {
    protected static RequestSpecification spec;

    @BeforeClass(alwaysRun = true)
    public void setupApi() {
        spec = new RequestSpecBuilder()
                .setBaseUri(Config.get("base.url.api"))
                .addHeader("Content-Type","application/json")
                .build();
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
}
