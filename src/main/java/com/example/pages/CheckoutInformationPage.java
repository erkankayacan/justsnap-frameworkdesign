package com.example.pages;

import com.example.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class CheckoutInformationPage extends BasePage {
    private final By firstName   = By.id("first-name");
    private final By lastName    = By.id("last-name");
    private final By postalCode  = By.id("postal-code");
    private final By continueBtn = By.id("continue");
    private final By errorBox    = By.cssSelector("h3[data-test='error'], [data-test='error'], .error-message-container");

    public CheckoutInformationPage(WebDriver driver) { super(driver);
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("checkout-step-one.html"),
                ExpectedConditions.visibilityOfElementLocated(firstName)
        ));
    }


    // fill+continue, return error ("" if page advanced)

    public String continueAndGetError(String fn, String ln, String zip) {
        type(By.id("first-name"), fn);
        type(By.id("last-name"), ln);
        type(By.id("postal-code"), zip);
        click(By.id("continue"));
        try {
            return new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(5))
                    .ignoring(org.openqa.selenium.NoSuchElementException.class)
                    .ignoring(org.openqa.selenium.StaleElementReferenceException.class)
                    .until(d -> {
                        var els = d.findElements(By.cssSelector("h3[data-test='error'], [data-test='error'], .error-message-container"));
                        return els.isEmpty() ? null : els.get(0).getText().trim();
                    });
        } catch (org.openqa.selenium.TimeoutException e) {
            return "";
        }
    }
    @Override
    public boolean isAt() {
        return driver.getCurrentUrl().contains("checkout-step-one.html")
                || !driver.findElements(By.id("first-name")).isEmpty();
    }

    public CheckoutOverviewPage fill(String fn, String ln, String zip) {
        type(firstName, fn);
        type(lastName, ln);
        type(postalCode, zip);
        click(continueBtn);
        new WebDriverWait(driver, Duration.ofSeconds(8)).until(ExpectedConditions.or(
                ExpectedConditions.urlContains("checkout-step-two.html"),
                ExpectedConditions.visibilityOfElementLocated(By.id("checkout_summary_container"))
        ));
        return new CheckoutOverviewPage(driver);
    }

    public boolean isAtStepOne() {
        return driver.getCurrentUrl().contains("checkout-step-one.html");
    }

}
