package com.example.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import com.example.pages.*;
public class LoginPage extends com.example.pages.BasePage {

    private final By username    = By.id("user-name");
    private final By password    = By.id("password");
    private final By loginBtn    = By.id("login-button");
    private final By errorBanner = By.cssSelector("[data-test='error'], h3[data-test='error']");

    public static LoginPage open() {
        LoginPage lp = new LoginPage();
        lp.driver.navigate().to("https://www.saucedemo.com/");
        lp.wait.until(ExpectedConditions.visibilityOfElementLocated(lp.username));
        return lp;
    }

    @Override
    public boolean isAt() {
        return isVisible(username) && isVisible(password);
    }

    public com.example.pages.BasePage login(String user, String pass) {
        type(username, user);
        type(password, pass);
        click(loginBtn);
        if (isVisible(errorBanner)) return this;
        return new ProductsPage();
    }

    public ProductsPage loginValid(String user, String pass) {
        type(username, user);
        type(password, pass);
        click(loginBtn);
        return new ProductsPage();
    }public ProductsPage loginExpectSuccess(String user, String pass) {
        return loginValid(user, pass);
    }

    public LoginPage loginInvalid(String user, String pass) {
        type(username, user);
        type(password, pass);
        click(loginBtn);
        return this;
    }

    public String getError() {
        if (!isPresent(errorBanner)) return "";
        return driver.findElement(errorBanner).getText();
    }
}
