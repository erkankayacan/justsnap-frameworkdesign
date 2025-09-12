package com.example.core;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public abstract class BasePage {
    protected final WebDriver driver;
    protected final WebDriverWait wait;
    private final By checkoutBtn = By.id("checkout");

    public BasePage(WebDriver driver) {
        this.driver = DriverFactory.get();
        this.wait = new WebDriverWait(this.driver, Duration.ofSeconds(10));
    }

    protected WebElement $(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected List<WebElement> findAll(By locator) {
        return driver.findElements(locator);
    }

    protected void click(By locator) {
        org.openqa.selenium.WebElement btn =
                new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(8))
                        .until(org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable(checkoutBtn));
        try { btn.click(); } catch (Exception e) {
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }}

    protected void type(By locator, String text) {
        WebElement e = $(locator);
        e.clear();
        e.sendKeys(text);
    }

    /** Expose driver to other page objects when needed (kept package-private). */
    WebDriver getDriver() { return this.driver; }

    public abstract boolean isAt();
}
