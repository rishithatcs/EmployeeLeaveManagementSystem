package com.elsm.automation.config;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;

/**
 * Thread-safe WebDriver factory and lifecycle manager.
 * Uses ThreadLocal to support parallel test execution.
 */
public class DriverManager {

    private static final Logger log = LogManager.getLogger(DriverManager.class);
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
    private static final ConfigReader config = ConfigReader.getInstance();

    private DriverManager() {}

    /**
     * Returns the WebDriver for the current thread, creating it if absent.
     */
    public static WebDriver getDriver() {
        if (driverThreadLocal.get() == null) {
            initDriver();
        }
        return driverThreadLocal.get();
    }

    /**
     * Initialises a new WebDriver instance for the current thread.
     */
    public static void initDriver() {
        String browser   = config.browser().toLowerCase();
        boolean headless = config.headless();
        WebDriver driver;

        log.info("Initialising {} driver (headless={})", browser, headless);

        switch (browser) {
            case "firefox" -> {
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions ffOpts = new FirefoxOptions();
                if (headless) ffOpts.addArguments("--headless");
                driver = new FirefoxDriver(ffOpts);
            }
            case "edge" -> {
                WebDriverManager.edgedriver().setup();
                EdgeOptions edgeOpts = new EdgeOptions();
                if (headless) edgeOpts.addArguments("--headless");
                driver = new EdgeDriver(edgeOpts);
            }
            default -> {
                WebDriverManager.chromedriver().setup();
                ChromeOptions chromeOpts = new ChromeOptions();
                if (headless) {
                    chromeOpts.addArguments("--headless=new");
                    chromeOpts.addArguments("--no-sandbox");
                    chromeOpts.addArguments("--disable-dev-shm-usage");
                }
                chromeOpts.addArguments("--window-size=1440,900");
                chromeOpts.addArguments("--disable-extensions");
                chromeOpts.addArguments("--remote-allow-origins=*");
                driver = new ChromeDriver(chromeOpts);
            }
        }

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(config.implicitWait()));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(config.pageLoadTimeout()));
        driver.manage().window().maximize();

        driverThreadLocal.set(driver);
        log.info("{} driver initialised successfully.", browser);
    }

    /**
     * Quits the WebDriver and removes it from ThreadLocal storage.
     */
    public static void quitDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            try {
                driver.quit();
                log.info("WebDriver quit successfully.");
            } catch (Exception e) {
                log.warn("Error while quitting WebDriver: {}", e.getMessage());
            } finally {
                driverThreadLocal.remove();
            }
        }
    }
}
