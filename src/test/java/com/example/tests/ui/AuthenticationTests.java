package com.example.tests.ui;

import com.example.core.BaseTest;
import com.example.core.DriverFactory;
import com.example.pages.LoginPage;
import com.example.pages.ProductsPage;
import com.example.utils.Config;
import com.example.utils.DataProviders;
import org.openqa.selenium.Cookie;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

public class AuthenticationTests extends BaseTest {

    @Test(description="Valid user login using config creds")
    public void validLogin(){
        ProductsPage products = LoginPage.open().loginExpectSuccess("standard_user","secret_sauce");
        assertTrue(products.isAt());
    }

    @Test(description="Invalid login shows error")
    public void invalidLoginWrongPassword(){
        LoginPage lp = LoginPage.open().login("standard_user", "wrong_pass");
        assertTrue(lp.getError().toLowerCase().contains("do not match"));
    }

    @Test(description="Locked out user scenario")
    public void lockedOutUser(){
        LoginPage lp = LoginPage.open().login("locked_out_user", "secret_sauce");
        assertTrue(lp.getError().toLowerCase().contains("locked out"));
    }

    @Test(description="Session timeout handling (simulated by clearing cookies)")
    public void sessionTimeoutHandling(){
        ProductsPage p = LoginPage.open().loginAs("standard_user","secret_sauce");
        assertTrue(p.isAt());
        DriverFactory.get().manage().deleteAllCookies();
        DriverFactory.get().navigate().to(Config.get("base.url.ui") + "/inventory.html");
        assertTrue(new LoginPage(DriverFactory.get()).isAt(), "Should be redirected to login after session invalidation");
    }

    @Test(
            dataProvider = "loginCsv",
            dataProviderClass = DataProviders.class,
            description = "Data-driven login via CSV"
    )
    public void loginDataCsv(String user, String pass, boolean valid) {
        LoginPage lp = LoginPage.open();

        if (valid) {
            ProductsPage pp = lp.loginExpectSuccess(user, pass);
            Assert.assertTrue(pp.isAt(), "Expected to land on inventory page");
        } else {
            lp.login(user, pass); // stay on login page
            String err = lp.getError();
            Assert.assertTrue(err != null && !err.isEmpty(), "Expected error for invalid credentials");
        }
    }
}
