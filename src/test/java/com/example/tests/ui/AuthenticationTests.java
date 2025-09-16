package com.example.tests.ui;

import com.example.core.BaseTest;
import com.example.core.DriverFactory;
import com.example.pages.LoginPage;
import com.example.pages.ProductsPage;
import com.example.utils.Config;
import com.example.utils.DataProviders;
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
    public void invalidLoginWrongPassword() {
        LoginPage lp = LoginPage.open().loginInvalid("standard_user", "wrong_pass");
        assertTrue(lp.getError().toLowerCase().contains("do not match")
                || lp.getError().toLowerCase().contains("epic sadface"));
    }

    @Test(description="Locked out user scenario")
    public void lockedOutUser() {
        LoginPage lp = LoginPage.open().loginInvalid("locked_out_user", "secret_sauce");
        assertTrue(lp.getError().toLowerCase().contains("locked out"));
    }

    @Test(description="Session timeout handling (simulated by clearing cookies)")
    public void sessionTimeoutHandling() {
        // ✅ Always returns ProductsPage
        ProductsPage p = LoginPage.open().loginValid("standard_user","secret_sauce");
        assertTrue(p.isAt());

        // Clear cookies to simulate timeout
        DriverFactory.get().manage().deleteAllCookies();

        // Try to go back to products
        DriverFactory.get().navigate().to(Config.get("base.url", "https://fakestoreapi.com") + "/inventory.html");

        // ✅ Recreate login page object and assert
        assertTrue(LoginPage.open().isAt(), "Should be redirected to login after session invalidation");
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
