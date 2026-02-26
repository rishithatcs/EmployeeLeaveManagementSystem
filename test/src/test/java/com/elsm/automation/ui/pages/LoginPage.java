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
 * Page Object for the Login page (/login).
 *
 * Maps to: frontendCapStone/src/pages/Login.js
 *
 * Key DOM facts from Login.js:
 *   - Email field:    <TextField name="username" label="Email" ...>
 *                     → MUI renders as <input name="username">
 *   - Password field: <TextField name="password" type="password" ...>
 *                     → MUI renders as <input name="password">
 *   - Submit button:  <Button type="submit">Login</Button>
 *                     → MUI renders as <button type="submit">
 *   - Error alert:    {error && <Alert severity="error">Invalid credentials</Alert>}
 *                     → MUI renders as <div role="alert">
 *   - Register link:  <Link to="/register">Register here</Link>
 *                     → Renders as <a href="/register">Register here</a>
 */
public class LoginPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // ── Element locators ──────────────────────────────────────────────

    // Login.js uses name="username" for the email field (confirmed from source)
    @FindBy(css = "input[name='username']")
    private WebElement emailField;

    @FindBy(css = "input[name='password']")
    private WebElement passwordField;

    @FindBy(css = "button[type='submit']")
    private WebElement loginButton;

    // MUI Alert renders with role="alert"
    private final By errorAlertLocator = By.cssSelector("[role='alert']");

    // "Register here" link — MUI Link renders as <a>
    @FindBy(linkText = "Register here")
    private WebElement registerLink;

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(
                ConfigReader.getInstance().explicitWait()));
        PageFactory.initElements(driver, this);
    }

    /** Navigate directly to /login and wait for the form to be visible. */
    public LoginPage navigateTo() {
        driver.get(ConfigReader.getInstance().uiBaseUrl() + "/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("button[type='submit']")));
        PageFactory.initElements(driver, this);
        return this;
    }

    public LoginPage enterEmail(String email) {
        wait.until(ExpectedConditions.visibilityOf(emailField));
        emailField.clear();
        if (!email.isEmpty()) {
            emailField.sendKeys(email);
        }
        return this;
    }

    public LoginPage enterPassword(String password) {
        wait.until(ExpectedConditions.visibilityOf(passwordField));
        passwordField.clear();
        if (!password.isEmpty()) {
            passwordField.sendKeys(password);
        }
        return this;
    }

    /**
     * Click the Login button. Returns a DashboardPage — the caller should check
     * {@link DashboardPage#isDashboardLoaded()} to confirm successful login.
     */
    public DashboardPage clickLoginButton() {
        loginButton.click();
        return new DashboardPage(driver);
    }

    /** Convenience: fill form and click submit. */
    public DashboardPage loginAs(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        return clickLoginButton();
    }

    /** Wait for and return the error alert text. */
    public String getErrorMessage() {
        WebElement alert = wait.until(
                ExpectedConditions.visibilityOfElementLocated(errorAlertLocator));
        return alert.getText();
    }

    /** Returns true if an error alert is visible on the page. */
    public boolean isErrorDisplayed() {
        try {
            return wait.until(
                    ExpectedConditions.visibilityOfElementLocated(errorAlertLocator))
                    .isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public RegisterPage clickRegisterLink() {
        wait.until(ExpectedConditions.elementToBeClickable(registerLink));
        registerLink.click();
        return new RegisterPage(driver);
    }

    public boolean isPageLoaded() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(loginButton)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isEmailFieldVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(emailField)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isPasswordFieldVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(passwordField)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}
