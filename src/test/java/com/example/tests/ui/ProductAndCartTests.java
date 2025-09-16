package com.example.tests.ui;

import com.example.core.BaseTest;
import com.example.pages.CartPage;
import com.example.pages.LoginPage;
import com.example.pages.ProductDetailsPage;
import com.example.pages.ProductsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class ProductAndCartTests extends BaseTest {

    @Test(description="View product catalog")
    public void viewProductCatalog() {
        ProductsPage p = LoginPage.open().loginValid("standard_user","secret_sauce");
        Assert.assertTrue(p.productCount() > 0);
    }


    @Test(description="Sort products A to Z and Z to A")
    public void sortProducts() {
        ProductsPage p = LoginPage.open().loginValid("standard_user","secret_sauce");

        p.sortByValue("Name (A to Z)");
        List<String> names = p.productNames();
        Assert.assertEquals(names, names.stream().sorted().toList(), "Should be sorted A-Z");

        p.sortByValue("Name (Z to A)");
        List<String> names2 = p.productNames();
        Assert.assertEquals(
                names2,
                names2.stream().sorted(java.util.Comparator.reverseOrder()).toList(),
                "Should be sorted Z-A"
        );
    }


    @Test(description="Sort prices low-high and high-low")
    public void sortByPrice() {
        ProductsPage p = LoginPage.open().loginValid("standard_user","secret_sauce");

        p.sortByValue("Price (low to high)");
        List<Double> prices = p.productPrices();
        Assert.assertEquals(prices, prices.stream().sorted().toList(), "Should be sorted low→high");

        p.sortByValue("Price (high to low)");
        List<Double> prices2 = p.productPrices();
        Assert.assertEquals(
                prices2,
                prices2.stream().sorted((a, b) -> Double.compare(b, a)).toList(),
                "Should be sorted high→low"
        );
    }


    @Test(description="Open product details by name")
    public void viewProductDetails() {
        ProductsPage p = LoginPage.open().loginValid("standard_user","secret_sauce");
        String name = p.productNames().get(0);

        ProductDetailsPage d = p.openProductByName(name);

        Assert.assertTrue(d.isAt(), "Product details page should be displayed");
        Assert.assertEquals(d.title(), name, "Title should match selected product");
    }


    @Test
    public void addProductsToCart() {
        ProductsPage p = LoginPage.open().loginExpectSuccess("standard_user","secret_sauce");

        p.clearCartOnProductsPage(); // clean state

        p.ensureInCartByName("Sauce Labs Backpack", java.time.Duration.ofSeconds(10));
        p.ensureInCartByName("Sauce Labs Bike Light", java.time.Duration.ofSeconds(10));

        CartPage c = p.clickCartAndGo();

        // wait until the cart shows exactly 2 items
        new org.openqa.selenium.support.ui.WebDriverWait(com.example.core.DriverFactory.get(),
                java.time.Duration.ofSeconds(8))
                .until(org.openqa.selenium.support.ui.ExpectedConditions
                        .numberOfElementsToBe(org.openqa.selenium.By.cssSelector(".cart_item"), 2));

        org.testng.Assert.assertEquals(c.itemCount(), 2);
    }

    @Test(description="Remove product from cart")
    public void removeFromCart(){
        ProductsPage p = LoginPage.open().loginValid("standard_user","secret_sauce");
        p.addToCartAtIndex(0);
        CartPage c = p.clickCartAndGo();
        c.removeFirst();
        Assert.assertEquals(c.itemCount(), 0);
    }

}
