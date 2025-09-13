package com.example.pages;

import com.example.core.BasePage;
import com.example.core.DriverFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class ProductsPage extends BasePage {
    private final By inventoryItem = By.className("inventory_item");
    private final By addToCartBtns = By.cssSelector("button.btn_inventory");
    private final By cartLink = By.cssSelector("a.shopping_cart_link");
    private final By cartBadge = By.cssSelector("span.shopping_cart_badge");
    private final By productNames = By.className("inventory_item_name");
    private final By productPrices = By.className("inventory_item_price");
    private final By sortDropdown = By.cssSelector("select.product_sort_container");
    private final WebDriver driver = DriverFactory.get();
    private final WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

    private By tileByName(String name) {
        // find the inventory_item whose child name matches exactly
        return By.xpath("//div[contains(@class,'inventory_item')][.//div[@class='inventory_item_name' and normalize-space()='" + name + "']]");
    }

    private By tileButton = By.cssSelector("button.btn_inventory");
    private By tileName   = By.className("inventory_item_name");


    public ProductsPage(WebDriver driver) {
        super(driver);
        wait.until(ExpectedConditions.visibilityOfElementLocated(inventoryItem));
    }



    @Override
    public boolean isAt() {
        return driver.getCurrentUrl().contains("inventory.html") &&
                !driver.findElements(inventoryItem).isEmpty();
    }

    public int productCount() {
        return driver.findElements(inventoryItem).size();
    }






    public List<Double> productPrices() {
        return driver.findElements(productPrices).stream().map(WebElement::getText)
                .map(s -> s.replace("$", "")).map(Double::parseDouble).collect(Collectors.toList());
    }
    public void clearCartOnProductsPage() {
        var removeBtns = driver.findElements(By.cssSelector("button.btn_inventory[data-test^='remove-']"));
        for (var b : removeBtns) {
            try { b.click(); } catch (Exception e) {
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", b);
            }
        }
        new WebDriverWait(driver, java.time.Duration.ofSeconds(5))
                .until(d -> d.findElements(By.cssSelector("button.btn_inventory[data-test^='remove-']")).isEmpty());
    }


    public List<String> productNames() {
        return driver.findElements(productNames)
                .stream()
                .map(WebElement::getText)
                .toList();
    }

    //  Helper: wait until inventory is loaded
    private void ensureInventoryIsReady() {
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.presenceOfAllElementsLocatedBy(productNames));
    }

    //  Sorting by visible text
    public void sortByVisibleText(String text) {
        ensureInventoryIsReady();
        WebElement dropdown = driver.findElement(sortDropdown);
        new Select(dropdown).selectByVisibleText(text);

        // Wait until list updates
        new WebDriverWait(driver, Duration.ofSeconds(6))
                .until(d -> !productNames().isEmpty());
    }
    public void sortBy(String value) {
        driver.findElement(sortDropdown).sendKeys(value);
    }

    public ProductDetailsPage openProductByName(String name) {
        WebElement link = driver.findElements(productNames).stream()
                .filter(e -> e.getText().equals(name)).findFirst().orElseThrow();
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", link);
        link.click();
        return new ProductDetailsPage(driver);
    }

    public CartPage clickCartAndGo() {
        WebElement link = driver.findElement(cartLink);
        try {
            ((org.openqa.selenium.JavascriptExecutor) driver)
                    .executeScript("arguments[0].scrollIntoView({block:'center'});", link);
            link.click();
        } catch (Exception e) {
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", link);
        }
        org.openqa.selenium.support.ui.WebDriverWait w =
                new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(6));
        try {
            w.until(org.openqa.selenium.support.ui.ExpectedConditions.urlContains("cart.html"));
        } catch (org.openqa.selenium.TimeoutException ignore) {
            driver.navigate().to("https://www.saucedemo.com/cart.html");
        }
        return new CartPage(driver);
    }


    public void addToCartAtIndex(int index) {
        // Wait for inventory grid
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.presenceOfAllElementsLocatedBy(inventoryItem));

        // Get all product tiles
        List<WebElement> items = driver.findElements(inventoryItem);
        if (index < 0 || index >= items.size()) {
            throw new IllegalArgumentException("Invalid product index: " + index);
        }

        // Find button inside the chosen item
        WebElement item = items.get(index);
        WebElement btn = item.findElement(By.cssSelector("button.btn_inventory"));

        // Only click if it's not already in cart
        String state = btn.getText().toLowerCase(); // text is "Add to cart" or "Remove"
        if (state.contains("add")) {
            btn.click();
            new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.textToBePresentInElement(btn, "Remove"));
        }
    }
    public int cartBadge() {
        java.util.List<org.openqa.selenium.WebElement> b = driver.findElements(cartBadge);
        return b.isEmpty() ? 0 : Integer.parseInt(b.get(0).getText());
    }



    private WebElement findTileByName(String name) {
        // try data-test name first
        List<WebElement> tiles = driver.findElements(By.xpath(
                "//div[contains(@class,'inventory_item')][.//*[@data-test='inventory-item-name' and normalize-space()='" + name + "']]"));
        if (!tiles.isEmpty()) return tiles.get(0);

        // fallback by class
        tiles = driver.findElements(By.xpath(
                "//div[contains(@class,'inventory_item')][.//div[contains(@class,'inventory_item_name') and normalize-space()='" + name + "']]"));
        if (!tiles.isEmpty()) return tiles.get(0);

        throw new NoSuchElementException("Product not found: " + name);
    }


    // Keep locators you already have:

    private final By inventoryContainer = By.id("inventory_container");

    // Small guard to be sure the grid is ready


    // Turn "Sauce Labs Backpack" -> "sauce-labs-backpack"
    private String toSlug(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }

    /** Ensure the product is in the cart (idempotent; no badge usage). */
    public void ensureInCartByName(String name, Duration timeout) {
        ensureInventoryIsReady();

        String slug = toSlug(name);
        By addBtn    = By.cssSelector("button[data-test='add-to-cart-" + slug + "']");
        By removeBtn = By.cssSelector("button[data-test='remove-" + slug + "']");

        WebDriverWait w = new WebDriverWait(driver, timeout);

        // already in cart?
        if (!driver.findElements(removeBtn).isEmpty()) return;

        // click add (scroll + fallback)
        WebElement btn = w.until(ExpectedConditions.presenceOfElementLocated(addBtn));
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
            btn.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }


    }



    public boolean isInCartByName(String name) {
        // Fast path: grid button state
        String slug = toSlug(name);
        if (!driver.findElements(By.cssSelector("button[data-test='remove-" + slug + "']")).isEmpty())
            return true;

        // Fallback: open cart, check, then go back
        String current = driver.getCurrentUrl();
        driver.findElement(By.id("shopping_cart_container")).click();
        boolean present = !driver.findElements(By.className("inventory_item_name"))
                .stream().filter(e -> name.equals(e.getText())).toList().isEmpty();
        driver.navigate().to(current); // return to products
        return present;
    }


}