package com.example.tests.ui;

import com.example.core.BaseTest;
import com.example.pages.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chromium.ChromiumDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;

public class CheckoutFlowTests extends BaseTest {



    @Test(description = "Complete purchase flow")
    public void completePurchase() {
        final String product = "Sauce Labs Backpack";

        // Login
        ProductsPage p = LoginPage.open().loginExpectSuccess("standard_user", "secret_sauce");

        // Add item if not already in cart
        p.ensureInCartByName(product, Duration.ofSeconds(15));

        // Go to cart
        CartPage c = p.clickCartAndGo();
        Assert.assertTrue(c.itemNames().contains(product), "Item should be in the cart");

        // Proceed to checkout
        CheckoutInformationPage info = c.checkout();
        Assert.assertTrue(info.isAtStepOne(), "Should be at checkout step one");

        // Fill checkout form
        CheckoutOverviewPage ov = info.fill("John", "Doe", "12345");
        Assert.assertTrue(ov.itemNames().contains(product), "Item should appear in overview");

        // Finish checkout
        CheckoutCompletePage done = ov.finish();
        Assert.assertTrue(
                done.header().toLowerCase().contains("thank you"),
                "Order confirmation should show 'Thank you'"
        );
    }
    @Test(description = "Checkout with empty cart shows validation on Step One")
    public void emptyCartCheckoutValidation() {
        // login
        ProductsPage p = LoginPage.open().loginExpectSuccess("standard_user","secret_sauce");

        // ensure cart is empty from the products grid
        p.clearCartOnProductsPage();

        // go to cart and assert empty
        CartPage c = p.clickCartAndGo();
        org.testng.Assert.assertEquals(c.itemCount(), 0, "Cart should be empty before checkout");

        // click Checkout -> SauceDemo navigates to Step One even if empty
        CheckoutInformationPage info = c.checkout();
        org.testng.Assert.assertTrue(info.isAtStepOne(), "Should land on checkout step one");

        // click Continue with missing data -> expect inline validation error
        String err = info.continueAndGetError("", "", "");
        org.testng.Assert.assertFalse(err.isEmpty(), "Expected validation error on missing fields");
        // Optionally assert message content:
        // org.testng.Assert.assertTrue(err.toLowerCase().contains("required"));
    }

    @Test(description="Payment info validation (missing fields)")
    public void paymentInfoValidation() {
        ProductsPage p = LoginPage.open().loginExpectSuccess("standard_user","secret_sauce");
        p.addToCartAtIndex(0);
        CartPage c = p.clickCartAndGo();
        CheckoutInformationPage info = c.checkout();

        String err = info.continueAndGetError("", "Doe", ""); // force errors

        // We should still be on step-one and see an error message
        org.testng.Assert.assertTrue(info.isAtStepOne(), "Should remain on checkout step one when invalid");
        org.testng.Assert.assertFalse(err.isEmpty(), "Expected validation error to be shown");
        // Optional stronger check:
        // org.testng.Assert.assertTrue(err.toLowerCase().contains("required"));
    }

    @Test(description = "Cart does not persist across sessions on SauceDemo")



        public void cartPersistenceAcrossSessions() {
            ProductsPage p = LoginPage.open().loginExpectSuccess("standard_user","secret_sauce");
            int before = p.cartBadge();
            p.addToCartAtIndex(0);                 // single add action
            int after = p.cartBadge();
            org.testng.Assert.assertEquals(after, before + 1, "Badge should increment by 1");

            com.example.core.DriverFactory.quit();
            com.example.core.DriverFactory.init(System.getProperty("browser", "chrome"));

            ProductsPage p2 = LoginPage.open().loginExpectSuccess("standard_user","secret_sauce");
            org.testng.Assert.assertEquals(p2.cartBadge(), 0);
        }
    }