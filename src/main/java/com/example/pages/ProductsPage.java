package com.example.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import java.util.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import com.example.pages.*;
public class ProductsPage extends com.example.pages.BasePage {

    private final By inventoryContainer = By.id("inventory_container");
    private final By inventoryItem = By.className("inventory_item");
    private final By addToCartBtns = By.cssSelector("button.btn_inventory");
    private final By cartLink      = By.cssSelector("a.shopping_cart_link");
    private final By cartBadge     = By.cssSelector("span.shopping_cart_badge");
    private final By productNames  = By.className("inventory_item_name");
    private final By productPrices = By.className("inventory_item_price");
    private final By sortDropdown  = By.cssSelector("select.product_sort_container");

    public ProductsPage() {
        new WebDriverWait(driver, Duration.ofSeconds(15))
                .until(ExpectedConditions.visibilityOfElementLocated(inventoryContainer));
    }

    @Override
    public boolean isAt() { return isVisible(inventoryContainer); }

    public int productCount() { return driver.findElements(inventoryItem).size(); }

    public List<String> productNames() {
        return driver.findElements(productNames).stream()
                .map(WebElement::getText).collect(Collectors.toList());
    }
    public CartPage clickCartAndGo() {
        click(cartLink);  // already defined in BasePage
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cart_contents_container")));
        return new CartPage();
    }
    public void clearCartOnProductsPage() {
        // Keep clicking until no "Remove" buttons are left
        List<WebElement> removeButtons = driver.findElements(By.xpath("//button[text()='Remove']"));
        while (!removeButtons.isEmpty()) {
            for (WebElement btn : removeButtons) {
                btn.click();
            }
            // Refresh list after clicking
            removeButtons = driver.findElements(By.xpath("//button[text()='Remove']"));
        }
    }



    public List<Double> productPrices() {
        return driver.findElements(productPrices).stream()
                .map(WebElement::getText)
                .map(t -> t.replace("$", "").trim())
                .map(Double::parseDouble)
                .collect(Collectors.toList());
    }

    public void addToCartAtIndex(int index) {
        List<WebElement> buttons = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfAllElementsLocatedBy(addToCartBtns));
        if (index < 0 || index >= buttons.size()) {
            throw new IllegalArgumentException("Invalid product index: " + index);
        }
        buttons.get(index).click();
    }





    public void ensureInCartByName(String productName, Duration timeout) {
        // 1. Find all product items
        for (WebElement item : driver.findElements(inventoryItem)) {
            WebElement nameEl = item.findElement(productNames);
            if (nameEl.getText().trim().equalsIgnoreCase(productName)) {
                WebElement btn = item.findElement(addToCartBtns);

                // 2. Click add if not already "Remove"
                if (btn.getText().equalsIgnoreCase("Add to cart")) {
                    btn.click();
                }

                // 3. Wait until cart badge increments (or just ensure it's visible)
                new WebDriverWait(driver, timeout)
                        .until(ExpectedConditions.visibilityOfElementLocated(cartBadge));

                return; // ✅ success
            }
        }
        throw new NoSuchElementException("Product not found: " + productName);
    }


    public void openCart() { click(cartLink); }

    public int cartBadgeCount() {
        if (!isPresent(cartBadge)) return 0;
        String txt = driver.findElement(cartBadge).getText().trim();
        return txt.isEmpty() ? 0 : Integer.parseInt(txt);
    }

    public void sortByValue(String visibleText) {
        WebElement dd = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(sortDropdown));
        new Select(dd).selectByVisibleText(visibleText);
    }

    public ProductDetailsPage openProductByName(String name) {
        List<WebElement> items = driver.findElements(inventoryItem);
        for (WebElement item : items) {
            WebElement n = item.findElement(productNames);
            if (n.getText().trim().equalsIgnoreCase(name.trim())) {
                n.click();
                return new ProductDetailsPage(); // ✅ navigate and return page object
            }
        }
        throw new NoSuchElementException("Product not found: " + name);
    }

}
