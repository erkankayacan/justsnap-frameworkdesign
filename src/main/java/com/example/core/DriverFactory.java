package com.example.core;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

public class DriverFactory {
    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    public static void init(String browser) {
        if (DRIVER.get() != null) return;
        switch (browser.toLowerCase()) {
            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions ff = new FirefoxOptions();
                DRIVER.set(new FirefoxDriver(ff));
                break;
            case "edge":
                WebDriverManager.edgedriver().setup();
                EdgeOptions edge = new EdgeOptions();
                DRIVER.set(new EdgeDriver(edge));
                break;
            default:
                WebDriverManager.chromedriver().setup();
                ChromeOptions options = new ChromeOptions();
                options.addArguments("--incognito");
                DRIVER.set(new ChromeDriver(options));
        }
        get().manage().window().maximize();
    }

    public static WebDriver get() {
        return DRIVER.get();
    }

    public static void quit() {
        if (DRIVER.get() != null) {
            get().quit();
            DRIVER.remove();
        }
    }
}
