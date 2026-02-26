package com.elsm.automation.config;

import com.elsm.automation.utils.ExtentReportManager;
import com.elsm.automation.utils.TestListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

/**
 * Base class for all Selenium UI tests.
 *
 * KEY DESIGN DECISION:
 *   @BeforeMethod creates a fresh WebDriver and navigates to the app root.
 *   It must NEVER throw an unchecked exception that would silently skip tests.
 *   If the frontend is not reachable we throw SkipException so that
 *   TestNG marks the test as SKIPPED with a meaningful message.
 *
 *   This class deliberately does NOT extend BaseApiTest and has NO
 *   @BeforeSuite of its own — UI tests are fully independent.
 */
@Listeners(TestListener.class)
public abstract class BaseUiTest {

    protected static final Logger log = LogManager.getLogger(BaseUiTest.class);
    protected static final ConfigReader config = ConfigReader.getInstance();

    protected WebDriver driver;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        DriverManager.initDriver();
        driver = DriverManager.getDriver();

        // Navigate to the app root and give React time to hydrate
        driver.get(config.uiBaseUrl());

        // Quick sanity check: if the page title is completely empty the app
        // is likely not running. Skip gracefully rather than failing cryptically.
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }

        String pageTitle = driver.getTitle();
        if (pageTitle == null) {
            throw new SkipException(
                "Frontend is not reachable at " + config.uiBaseUrl() +
                " — skipping UI test. Start the React dev-server (npm start) and re-run.");
        }

        log.info("Browser opened. Navigated to {} (title: '{}')", config.uiBaseUrl(), pageTitle);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        DriverManager.quitDriver();
        ExtentReportManager.removeTest();
        log.info("Browser closed.");
    }

    @AfterSuite(alwaysRun = true)
    public void suiteTearDown() {
        ExtentReportManager.flush();
    }
}
