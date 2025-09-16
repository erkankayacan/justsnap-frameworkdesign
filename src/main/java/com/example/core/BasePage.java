package com.example.pages;

import com.example.core.DriverFactory;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;

public abstract class BasePage {
    protected WebDriver driver;
    protected WebDriverWait wait;

    /** ✅ No-arg path (works with pages that DON'T pass a driver) */
    protected BasePage() {
        this.init(DriverFactory.get());
    }

    /** ✅ DI path (works with pages that DO pass a driver via super(driver)) */
    protected BasePage(WebDriver driver) {
        this.init(driver);
    }

    private void init(WebDriver d) {
        this.driver = d;
        this.wait = new WebDriverWait(d, Duration.ofSeconds(10));
    }

    protected void type(By locator, CharSequence text) {
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
        el.clear();
        el.sendKeys(text);
    }

    protected void click(By locator) {
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
        el.click();
    }

    protected boolean isVisible(By locator) {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    protected boolean isPresent(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public abstract boolean isAt();
}
