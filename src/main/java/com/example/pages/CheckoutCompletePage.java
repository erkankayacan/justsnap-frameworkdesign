package com.example.pages;

import com.example.core.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class CheckoutCompletePage extends BasePage {
    private final By header = By.className("complete-header");
    public CheckoutCompletePage(WebDriver driver) { super(driver);
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("checkout-complete.html"),
                ExpectedConditions.visibilityOfElementLocated(header)
        ));
    }
    @Override public boolean isAt(){ return driver.findElements(header).size() > 0; }
    public String header(){ return $(header).getText(); }

    }