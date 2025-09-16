package com.example.pages;

import com.example.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public class CheckoutOverviewPage extends BasePage {
    private final By finishBtn = By.id("finish");
    private final By summaryContainer = By.id("checkout_summary_container");
    private final By itemName         = By.className("inventory_item_name");

    @Override public boolean isAt(){ return driver.findElements(finishBtn).size() > 0; }


    public CheckoutOverviewPage(WebDriver driver) { super(driver);
        wait.until(ExpectedConditions.visibilityOfElementLocated(summaryContainer));
    }

    public List<String> itemNames() {
        WebElement el;
        return driver.findElements(itemName).stream().map(WebElement::getText).toList();
    }

    public CheckoutCompletePage finish() {
        click(finishBtn);
        return new CheckoutCompletePage(driver);
    }
}
