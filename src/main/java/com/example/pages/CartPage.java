package com.example.pages;

import com.example.core.BasePage;
import com.example.core.DriverFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;
import java.util.stream.Collectors;

public class CartPage extends BasePage {
    private final By cartItem = By.className("cart_item");

    private final By cartItems = By.cssSelector(".cart_item");
    private final By removeBtn = By.cssSelector("button.cart_button");
    private final By continueShopping = By.id("continue-shopping");
    private final By checkoutBtn = By.id("checkout");

    private final By itemName  = By.className("inventory_item_name");

    public CartPage(WebDriver driver) { super(driver);
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("cart.html"),
                ExpectedConditions.presenceOfElementLocated(cartItems)
        ));
    }


    @Override
    public boolean isAt() {
        return driver.findElements(cartItem).size() >= 0;
    }



    public int itemCount() { return DriverFactory.get().findElements(cartItems).size(); }

    public void removeFirst() {
        if (!driver.findElements(removeBtn).isEmpty()) {
            driver.findElements(removeBtn).get(0).click();
        }
    }



    public ProductsPage continueShopping() {
        click(continueShopping);
        return new ProductsPage(driver);
    }
    public List<String> itemNames() {
        return driver.findElements(itemName)
                .stream()
                .map(WebElement::getText)
                .toList();
    }

    public CheckoutInformationPage checkout() {
        // Click (uses BasePage.click -> waits & JS fallback)
        click(checkoutBtn);

        // Wait until Step One is really loaded (URL or first-name field)
        new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(10))
                .until(org.openqa.selenium.support.ui.ExpectedConditions.or(
                        org.openqa.selenium.support.ui.ExpectedConditions.urlContains("checkout-step-one.html"),
                        org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated(org.openqa.selenium.By.id("first-name"))
                ));

        return new CheckoutInformationPage(driver);
    }


}