package com.example.pages;

import com.example.core.BasePage;
import com.example.core.DriverFactory;
import com.example.utils.Config;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;

public class LoginPage extends BasePage {
    private final By username = By.id("user-name");
    private final By password = By.id("password");
    private final By loginBtn = By.id("login-button");
    private final By errorMsg = By.cssSelector("[data-test='error']");

    public LoginPage(WebDriver driver) { super(driver); }

    public static LoginPage open() {
        DriverFactory.get().navigate().to(Config.get("base.url.ui"));
        return new LoginPage(DriverFactory.get());
    }

    public ProductsPage loginAs(String user, String pass) {
        type(username, user);
        type(password, pass);
        click(loginBtn);
        return new ProductsPage(driver);
    }

    public String getError() {
        return $(errorMsg).getText();
    }

    @Override
    public boolean isAt() {
        return $(loginBtn).isDisplayed();
    }
    public ProductsPage loginExpectSuccess(String user, String pass) {
        type(username, user);
        type(password, pass);
        click(loginBtn);

        // Wait up to 20s for either success (URL contains inventory.html) or error banner
        wait.withTimeout(Duration.ofSeconds(20)).until(ExpectedConditions.or(
                ExpectedConditions.urlContains("inventory.html"),
                ExpectedConditions.visibilityOfElementLocated(errorMsg)
        ));

        if (!driver.findElements(errorMsg).isEmpty()) {
            throw new RuntimeException("Login failed: " + getError());
        }
        return new ProductsPage(driver);
    }




    // For negative tests:
    public LoginPage login(String user, String pass) {
        type(username, user);
        type(password, pass);
        click(loginBtn);
        return this;
    }

}
