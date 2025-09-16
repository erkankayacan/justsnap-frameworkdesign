package com.example.tests.api;

import com.example.api.ApiBase;
import com.example.api.ProductClient;
import com.example.api.clients.UserClient;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class SmokeApiTests extends ApiBase {

    private final ProductClient products = new ProductClient();
    private final UserClient users = new UserClient();

    @Test
    public void productById() {
        Response r = products.getById(1);
        assertThat(r.statusCode(), is(200));
        assertThat(r.jsonPath().getInt("id"), is(1));
        assertThat(r.jsonPath().getString("title"), not(isEmptyOrNullString()));
    }

    @Test
    public void authLogin() {
        // fakestore known creds
        Response r = users.authenticate("mor_2314", "83r5^_");
        assertThat(r.statusCode(), is(201));
        assertThat(r.jsonPath().getString("token"), not(isEmptyOrNullString()));
    }
}

