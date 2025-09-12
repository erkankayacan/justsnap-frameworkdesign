package com.example.pages;

import com.example.core.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class ProductDetailsPage extends BasePage {
    private final By title = By.className("inventory_details_name");
    private final By price = By.className("inventory_details_price");
    private final By addToCart = By.cssSelector("button.btn_primary.btn_inventory");
    private final By backBtn = By.id("back-to-products");

    public ProductDetailsPage(WebDriver driver) { super(driver); }


    @Override
    public boolean isAt() {
        return driver.getCurrentUrl().contains("inventory-item.html")
                || !driver.findElements(By.className("inventory_details_name")).isEmpty();
    }

    public String title() { return $(title).getText(); }
    public double price() { return Double.parseDouble($(price).getText().replace("$","")); }
    public void addToCart(){ click(addToCart); }
    public ProductsPage back(){ click(backBtn); return new ProductsPage(driver); }

}
