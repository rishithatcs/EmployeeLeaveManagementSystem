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
 * Page Object for the Leave Requests page (/leave-requests).
 *
 * Maps to: frontendCapStone/src/pages/LeaveRequests.js
 *
 * Key DOM facts from LeaveRequests.js:
 *   - Page heading:       <Typography variant="h5">Your Leave Requests</Typography>
 *   - Apply button:       <Button variant="contained">Apply for Leave</Button>
 *   - Modal:              <Modal open={modalOpen}> containing a Card
 *   - Leave Type select:  <TextField select name="leaveType" label="Leave Type">
 *   - From date:          <TextField type="date" name="startDate" label="From">
 *   - To date:            <TextField type="date" name="endDate" label="To">
 *   - Reason textarea:    <TextField multiline name="notes" label="Reason">
 *   - Submit button:      <Button onClick={handleSubmit}>Submit</Button>
 *   - Cancel button:      <Button onClick={() => setModalOpen(false)}>Cancel</Button>
 *   - Error alert:        {error && <Alert severity="error">}
 *   - Success alert:      {success && <Alert severity="success">}
 */
public class LeaveRequestsPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // ── Locators ──────────────────────────────────────────────────────

    // Page heading
    private final By pageHeadingLocator =
            By.xpath("//*[contains(text(),'Your Leave Requests')]");

    // "Apply for Leave" button (opens modal)
    @FindBy(xpath = "//button[contains(text(),'Apply for Leave')]")
    private WebElement applyForLeaveButton;

    // Modal: Leave Type MUI select trigger
    private final By leaveTypeSelectLocator =
            By.cssSelector("[name='leaveType']");

    // Modal: Date fields
    private final By startDateLocator  = By.cssSelector("input[name='startDate']");
    private final By endDateLocator    = By.cssSelector("input[name='endDate']");

    // Modal: Notes/Reason textarea
    private final By notesLocator = By.cssSelector("textarea[name='notes']");

    // Modal: Submit button
    private final By submitButtonLocator =
            By.xpath("//button[contains(text(),'Submit')]");

    // Modal: Cancel button
    private final By cancelButtonLocator =
            By.xpath("//button[contains(text(),'Cancel')]");

    // Error and success alerts inside the modal
    private final By errorAlertLocator   = By.cssSelector("[role='alert']");
    private final By successAlertLocator = By.cssSelector("[role='alert']");

    public LeaveRequestsPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(
                ConfigReader.getInstance().explicitWait()));
        PageFactory.initElements(driver, this);
    }

    public LeaveRequestsPage navigateTo() {
        driver.get(ConfigReader.getInstance().uiBaseUrl() + "/leave-requests");
        wait.until(ExpectedConditions.visibilityOfElementLocated(pageHeadingLocator));
        PageFactory.initElements(driver, this);
        return this;
    }

    public boolean isPageLoaded() {
        try {
            return wait.until(
                    ExpectedConditions.visibilityOfElementLocated(pageHeadingLocator))
                    .isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /** Click "Apply for Leave" to open the modal. */
    public LeaveRequestsPage clickApplyForLeave() {
        wait.until(ExpectedConditions.elementToBeClickable(applyForLeaveButton));
        applyForLeaveButton.click();
        // Wait for the modal to appear (MUI Modal renders an inner Card)
        wait.until(ExpectedConditions.visibilityOfElementLocated(submitButtonLocator));
        return this;
    }

    public boolean isModalVisible() {
        try {
            return wait.until(
                    ExpectedConditions.visibilityOfElementLocated(submitButtonLocator))
                    .isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Select a leave type in the MUI Select inside the modal.
     * @param leaveTypeValue e.g. "SICK", "VACATION", "PAID", "UNPAID"
     */
    public LeaveRequestsPage selectLeaveType(String leaveTypeValue) {
        // Click the MUI Select trigger
        WebElement selectTrigger = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.cssSelector(".MuiSelect-select[name='leaveType'], " +
                                       "input[name='leaveType'], " +
                                       "[id*='leaveType']")));
        selectTrigger.click();

        // Click the matching MenuItem
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//li[@data-value='" + leaveTypeValue + "']"))).click();
        return this;
    }

    public LeaveRequestsPage enterStartDate(String date) {
        WebElement field = wait.until(
                ExpectedConditions.elementToBeClickable(startDateLocator));
        field.clear();
        field.sendKeys(date);
        return this;
    }

    public LeaveRequestsPage enterEndDate(String date) {
        WebElement field = wait.until(
                ExpectedConditions.elementToBeClickable(endDateLocator));
        field.clear();
        field.sendKeys(date);
        return this;
    }

    public LeaveRequestsPage enterNotes(String notes) {
        WebElement field = wait.until(
                ExpectedConditions.elementToBeClickable(notesLocator));
        field.clear();
        field.sendKeys(notes);
        return this;
    }

    public void clickSubmit() {
        wait.until(ExpectedConditions.elementToBeClickable(submitButtonLocator)).click();
    }

    public void clickCancel() {
        wait.until(ExpectedConditions.elementToBeClickable(cancelButtonLocator)).click();
    }

    public boolean isErrorDisplayed() {
        try {
            return wait.until(
                    ExpectedConditions.visibilityOfElementLocated(errorAlertLocator))
                    .isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isSuccessDisplayed() {
        try {
            // Wait a bit for the async call to complete
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
