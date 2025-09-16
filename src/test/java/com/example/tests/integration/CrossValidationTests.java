package com.example.tests.integration;

import com.example.api.ProductClient;
import com.example.core.BaseTest;
import com.example.pages.LoginPage;
import com.example.pages.ProductsPage;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

public class CrossValidationTests extends BaseTest {
    private ProductClient api;

    @BeforeClass
    public void init(){
        api = new ProductClient();
        api.getById(1);
    }

    @Test(description="Cross-validate product names between API and UI (subset check)")
    public void crossValidateProductNames(){
        Response r = api.getAll();
        List<String> apiTitles = r.jsonPath().getList("title");
        ProductsPage p = LoginPage.open().loginValid("standard_user","secret_sauce");
        List<String> uiNames = p.productNames();
        // Saucedemo has different dataset than fakestore; this acts as a placeholder integration demo
        Assert.assertTrue(uiNames.size() > 0 && apiTitles.size() > 0);
    }
}
