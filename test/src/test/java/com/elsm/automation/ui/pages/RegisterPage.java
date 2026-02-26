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
 * Page Object for the Register page (/register).
 *
 * Maps to: frontendCapStone/src/pages/Register.js
 *
 * Key DOM facts from Register.js:
 *   - Username field: <TextField name="username" label="Username">  → input[name='username']
 *   - Email field:    <TextField name="email"    label="Email">     → input[name='email']
 *   - Password field: <TextField name="password" type="password">   → input[name='password']
 *   - Role select:    <TextField select name="role" label="Role">   → MUI Select, div[role='button'] or input
 *   - Submit button:  <Button type="submit">Register</Button>       → button[type='submit']
 *   - Error alert:    {error && <Alert severity="error">...}        → [role='alert']
 */
public class RegisterPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // ── Element locators ──────────────────────────────────────────────

    @FindBy(css = "input[name='username']")
    private WebElement usernameField;

    @FindBy(css = "input[name='email']")
    private WebElement emailField;

    @FindBy(css = "input[name='password']")
    private WebElement passwordField;

    @FindBy(css = "button[type='submit']")
    private WebElement registerButton;

    private final By errorAlertLocator = By.cssSelector("[role='alert']");

    // MUI Select trigger element for the Role dropdown
    private final By roleSelectLocator = By.id("mui-component-select-role");
    // Fallback: look for the input hidden behind MUI Select
    private final By roleInputLocator  = By.cssSelector("input[name='role']");

    // MenuItem for EMPLOYEE and MANAGER roles
    private final By employeeOptionLocator = By.xpath("//li[@data-value='EMPLOYEE']");
    private final By managerOptionLocator  = By.xpath("//li[@data-value='MANAGER']");

    public RegisterPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(
                ConfigReader.getInstance().explicitWait()));
        PageFactory.initElements(driver, this);
    }

    public RegisterPage navigateTo() {
        driver.get(ConfigReader.getInstance().uiBaseUrl() + "/register");
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("button[type='submit']")));
        PageFactory.initElements(driver, this);
        return this;
    }

    public RegisterPage enterUsername(String username) {
        wait.until(ExpectedConditions.visibilityOf(usernameField));
        usernameField.clear();
        usernameField.sendKeys(username);
        return this;
    }

    public RegisterPage enterEmail(String email) {
        wait.until(ExpectedConditions.visibilityOf(emailField));
        emailField.clear();
        emailField.sendKeys(email);
        return this;
    }

    public RegisterPage enterPassword(String password) {
        wait.until(ExpectedConditions.visibilityOf(passwordField));
        passwordField.clear();
        passwordField.sendKeys(password);
        return this;
    }

    /**
     * Select a role from the MUI dropdown.
     * MUI Select renders as a div that opens a listbox on click.
     * @param role "EMPLOYEE" or "MANAGER"
     */
    public RegisterPage selectRole(String role) {
        try {
            // Try clicking the MUI Select trigger (div[role='button'] or similar)
            WebElement selectTrigger = wait.until(
                    ExpectedConditions.elementToBeClickable(
                            By.cssSelector("[name='role']").toString().contains("input")
                                    ? By.cssSelector("input[name='role']")
                                    : By.cssSelector(".MuiSelect-select")));
            selectTrigger.click();
        } catch (Exception e) {
            // Fallback: click the visible select element directly
            try {
                driver.findElement(By.cssSelector(".MuiSelect-select")).click();
            } catch (Exception ex) {
                // Last resort: try the label area
                driver.findElement(By.xpath("//*[contains(@class,'MuiSelect')]")).click();
            }
        }

        // Now click the appropriate option
        By optionLocator = "MANAGER".equalsIgnoreCase(role) ? managerOptionLocator : employeeOptionLocator;
        wait.until(ExpectedConditions.elementToBeClickable(optionLocator)).click();
        return this;
    }

    /** Full registration action. */
    public void register(String username, String email, String password, String role) {
        enterUsername(username);
        enterEmail(email);
        enterPassword(password);
        selectRole(role);
        clickRegisterButton();
    }

    public void clickRegisterButton() {
        wait.until(ExpectedConditions.elementToBeClickable(registerButton));
        registerButton.click();
    }

    public boolean isPageLoaded() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(registerButton)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
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

    public String getErrorMessage() {
        WebElement alert = wait.until(
                ExpectedConditions.visibilityOfElementLocated(errorAlertLocator));
        return alert.getText();
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}
