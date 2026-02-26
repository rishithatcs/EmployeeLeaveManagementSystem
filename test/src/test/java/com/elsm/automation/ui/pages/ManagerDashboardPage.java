package com.elsm.automation.ui.pages;

import com.elsm.automation.config.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page Object for the Manager Dashboard (/manager).
 *
 * Maps to: frontendCapStone/src/pages/ManagerDashboard.js
 *
 * Key DOM facts from ManagerDashboard.js:
 *   - Page heading:    <Typography variant="h5">Pending Leave Requests</Typography>
 *   - Approve button:  <Button color="success">Approve</Button>
 *   - Reject button:   <Button color="error">Reject</Button>
 *   - Success alert:   {success && <Alert severity="success">}
 *   - Error alert:     {error && <Alert severity="error">}
 */
public class ManagerDashboardPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By headingLocator =
            By.xpath("//*[contains(text(),'Pending Leave Requests')]");

    private final By approveButtonLocator =
            By.xpath("//button[contains(text(),'Approve')]");

    private final By rejectButtonLocator =
            By.xpath("//button[contains(text(),'Reject')]");

    private final By successAlertLocator = By.cssSelector("[role='alert']");

    public ManagerDashboardPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(
                ConfigReader.getInstance().explicitWait()));
        PageFactory.initElements(driver, this);
    }

    public ManagerDashboardPage navigateTo() {
        driver.get(ConfigReader.getInstance().uiBaseUrl() + "/manager");
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
            WebElement h = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(headingLocator));
            return h.getText();
        } catch (Exception e) {
            return "";
        }
    }

    public boolean isApproveButtonVisible() {
        try {
            return driver.findElements(approveButtonLocator).stream()
                    .anyMatch(WebElement::isDisplayed);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isRejectButtonVisible() {
        try {
            return driver.findElements(rejectButtonLocator).stream()
                    .anyMatch(WebElement::isDisplayed);
        } catch (Exception e) {
            return false;
        }
    }

    /** Click the first visible Approve button. */
    public void clickFirstApprove() {
        wait.until(ExpectedConditions.elementToBeClickable(approveButtonLocator)).click();
    }

    /** Click the first visible Reject button. */
    public void clickFirstReject() {
        wait.until(ExpectedConditions.elementToBeClickable(rejectButtonLocator)).click();
    }

    public boolean isSuccessAlertDisplayed() {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        try {
            return wait.until(
                    ExpectedConditions.visibilityOfElementLocated(successAlertLocator))
                    .isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}
