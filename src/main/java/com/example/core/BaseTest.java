package com.example.core;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.example.utils.Config;
import com.example.utils.ScreenshotUtil;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.lang.reflect.Method;

public class BaseTest {
    protected static ExtentReports extent;  // make static so it's shared
    protected ExtentTest test;

    @Parameters({"env","browser"})
    @BeforeSuite(alwaysRun = true)
    public void setupSuite(@Optional("DEV") String env, @Optional("chrome") String browser) {
        Config.load(env);

        if (extent == null) {  // initialize only once
            ExtentSparkReporter spark = new ExtentSparkReporter("target/extent-report.html");
            extent = new ExtentReports();
            extent.attachReporter(spark);
        }
    }

    @Parameters({"browser"})
    @BeforeMethod(alwaysRun = true)
    public void beforeTest(@Optional("chrome") String browser, Method method) {
        DriverFactory.init(browser);

        // Ensure extent is not null
        if (extent == null) {
            ExtentSparkReporter spark = new ExtentSparkReporter("target/extent-report.html");
            extent = new ExtentReports();
            extent.attachReporter(spark);
        }

        test = extent.createTest(method.getName());
    }

    @AfterMethod(alwaysRun = true)
    public void afterTest(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            String path = ScreenshotUtil.takeScreenshot(DriverFactory.get(), result.getName());
            test.addScreenCaptureFromPath(path);
            test.fail(result.getThrowable());
        }
        DriverFactory.quit();
    }

    @AfterSuite(alwaysRun = true)
    public void tearDownSuite() {
        if (extent != null) extent.flush();
    }
}