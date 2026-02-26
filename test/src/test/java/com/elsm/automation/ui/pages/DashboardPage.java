package com.elsm.automation.ui.pages;

import com.elsm.automation.config.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page Object for the Dashboard page (/).
 *
 * Maps to: frontendCapStone/src/pages/Dashboard.js
 *
 * Key DOM facts from Dashboard.js:
 *   - Welcome text: <Typography variant="h4">Welcome, <span>{user?.username}</span></Typography>
 *     NOTE: AuthContext stores the raw API response as 'user'.
 *     JwtResponse has field 'name' (not 'username'), so user.name = "Employee User"
 *     and user.username = undefined — the welcome text renders as "Welcome, "
 *     We assert the h4 element contains "Welcome" (case-insensitive).
 *   - Logout button: <Button ...>Logout</Button>
 *   - Leave Balance section: <Typography variant="h6">Leave Balance</Typography>
 *   - Leave History section: <Typography variant="h6">Leave History</Typography>
 *   - Apply for Leave button (EMPLOYEE only): <Button ...>Apply for Leave</Button>
 *   - Pending Leave Requests (MANAGER only): <Typography variant="h5">Pending Leave Requests</Typography>
 */
public class DashboardPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // ── Locators ──────────────────────────────────────────────────────

    // Welcome heading — MUI Typography h4
    private final By welcomeHeadingLocator = By.cssSelector("h4");

    // Logout button
    @FindBy(xpath = "//button[contains(text(),'Logout')]")
    private WebElement logoutButton;

    // Leave Balance card heading
    private final By leaveBalanceLocator =
            By.xpath("//*[contains(text(),'Leave Balance')]");

    // Leave History card heading
    private final By leaveHistoryLocator =
            By.xpath("//*[contains(text(),'Leave History')]");

    // "Apply for Leave" button (only visible for EMPLOYEE role)
    private final By applyLeaveButtonLocator =
            By.xpath("//button[contains(text(),'Apply for Leave')]");

    // "Pending Leave Requests" section heading (only visible for MANAGER role)
    private final By pendingRequestsLocator =
            By.xpath("//*[contains(text(),'Pending Leave Requests')]");

    public DashboardPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(
                ConfigReader.getInstance().explicitWait()));
        PageFactory.initElements(driver, this);
    }

    /**
     * Returns true once the dashboard has finished loading.
     * We wait for the Logout button to become visible as the reliable indicator
     * that the authenticated Dashboard component has rendered.
     */
    public boolean isDashboardLoaded() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//button[contains(text(),'Logout')]")));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns the text of the h4 welcome heading.
     * Dashboard.js renders: "Welcome, {user?.username}"
     * Since JwtResponse uses field 'name' (not 'username'), the span may be empty,
     * but the h4 will always contain "Welcome".
     */
    public String getWelcomeText() {
        try {
            WebElement h4 = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(welcomeHeadingLocator));
            return h4.getText();
        } catch (Exception e) {
            return "";
        }
    }

    public boolean isLeaveBalanceSectionVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(leaveBalanceLocator))
                    .isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isLeaveHistorySectionVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(leaveHistoryLocator))
                    .isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isApplyLeaveButtonVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(applyLeaveButtonLocator))
                    .isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isPendingRequestsSectionVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(pendingRequestsLocator))
                    .isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /** Click "Apply for Leave" and return the LeaveRequestsPage. */
    public LeaveRequestsPage clickApplyForLeave() {
        wait.until(ExpectedConditions.elementToBeClickable(applyLeaveButtonLocator)).click();
        return new LeaveRequestsPage(driver);
    }

    /** Click Logout and return the LoginPage. */
    public LoginPage clickLogout() {
        wait.until(ExpectedConditions.elementToBeClickable(logoutButton));
        logoutButton.click();
        return new LoginPage(driver);
    }

    /** Get the current URL. */
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}
