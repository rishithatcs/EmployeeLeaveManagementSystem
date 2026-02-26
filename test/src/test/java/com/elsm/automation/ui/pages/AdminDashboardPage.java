package com.elsm.automation.ui.pages;

import com.elsm.automation.config.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page Object for the Admin Dashboard page (/admin).
 *
 * Maps to: frontendCapStone/src/pages/AdminDashboard.js
 *
 * Key DOM facts from AdminDashboard.js:
 *   - Page heading: <h2>Admin Dashboard</h2>
 *   - Content:      <div>User Management (employees, managers)</div>
 *                   <div>Leave Policies</div>
 */
public class AdminDashboardPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // AdminDashboard.js renders a plain <h2>, not MUI Typography
    private final By headingLocator = By.xpath("//h2[contains(text(),'Admin Dashboard')]");

    public AdminDashboardPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(
                ConfigReader.getInstance().explicitWait()));
        PageFactory.initElements(driver, this);
    }

    public AdminDashboardPage navigateTo() {
        driver.get(ConfigReader.getInstance().uiBaseUrl() + "/admin");
        wait.until(ExpectedConditions.visibilityOfElementLocated(headingLocator));
        return this;
    }

    public boolean isPageLoaded() {
        try {
            return wait.until(
                    ExpectedConditions.visibilityOfElementLocated(headingLocator))
                    .isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getHeadingText() {
        try {
            return wait.until(
                    ExpectedConditions.visibilityOfElementLocated(headingLocator))
                    .getText();
        } catch (Exception e) {
            return "";
        }
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}
