package com.example.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;


public class ProductDetailsPage extends com.example.pages.BasePage {
    private final By title = By.className("inventory_details_name");
    private final By price = By.className("inventory_details_price");
    private final By addToCart = By.cssSelector("button.btn_primary.btn_inventory");
    private final By backBtn = By.id("back-to-products");




        public ProductDetailsPage() {
            super(); // calls BasePage constructor first
            // ✅ safe to use driver/wait after super()
            wait.until(ExpectedConditions.visibilityOfElementLocated(title));
        }
    public String title() {
        return driver.findElement(title).getText();
    }



    @Override
    public boolean isAt() {
        return driver.getCurrentUrl().contains("inventory-item.html")
                || !driver.findElements(By.className("inventory_details_name")).isEmpty();
    }

    public String getTitle() {
        return driver.findElement(title).getText();
    }

    public String getPrice() {
        return driver.findElement(price).getText();
    }
    public void addToCart(){ click(addToCart); }
    public ProductsPage back(){ click(backBtn); return new ProductsPage(); }

}
