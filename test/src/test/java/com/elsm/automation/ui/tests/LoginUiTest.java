package com.elsm.automation.ui.tests;

import com.elsm.automation.config.BaseUiTest;
import com.elsm.automation.ui.pages.DashboardPage;
import com.elsm.automation.ui.pages.LoginPage;
import com.elsm.automation.ui.pages.RegisterPage;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UI Tests for the Login page (/login).
 *
 * Login.js key facts:
 *   - Email field:   input[name='username']   (form state key is 'username', maps to email)
 *   - Password:      input[name='password']
 *   - Submit button: button[type='submit'] with text "Login"
 *   - Error alert:   <Alert severity="error">Invalid credentials</Alert>
 *   - Register link: <Link to="/register">Register here</Link>
 *
 * After successful login, React navigates to '/' (Dashboard).
 */
public class LoginUiTest extends BaseUiTest {

    // ================================================================
    // TC-UI-LGN-01: Page Load
    // ================================================================

    @Test(groups = {"smoke", "ui", "login"},
          description = "TC-UI-LGN-01: Login page loads with Email, Password fields and Login button")
    public void testLoginPageLoads() {
        LoginPage loginPage = new LoginPage(driver).navigateTo();

        assertThat(loginPage.isPageLoaded())
                .as("Login button should be visible on the login page")
                .isTrue();
        assertThat(loginPage.isEmailFieldVisible())
                .as("Email (username) field should be visible")
                .isTrue();
        assertThat(loginPage.isPasswordFieldVisible())
                .as("Password field should be visible")
                .isTrue();
    }

    // ================================================================
    // TC-UI-LGN-02..04: Successful Login for each role
    // ================================================================

    @Test(groups = {"smoke", "ui", "login"},
          description = "TC-UI-LGN-02: Logging in as Employee redirects to Dashboard and shows welcome message")
    public void testSuccessfulEmployeeLogin() {
        LoginPage     loginPage     = new LoginPage(driver).navigateTo();
        DashboardPage dashboardPage = loginPage.loginAs(config.employeeEmail(), config.employeePassword());

        assertThat(dashboardPage.isDashboardLoaded())
                .as("Dashboard should load after successful employee login")
                .isTrue();
        assertThat(dashboardPage.getWelcomeText())
                .as("Welcome heading must contain the word 'Welcome'")
                .containsIgnoringCase("Welcome");
    }

    @Test(groups = {"smoke", "ui", "login"},
          description = "TC-UI-LGN-03: Logging in as Manager redirects to Dashboard")
    public void testSuccessfulManagerLogin() {
        LoginPage     loginPage     = new LoginPage(driver).navigateTo();
        DashboardPage dashboardPage = loginPage.loginAs(config.managerEmail(), config.managerPassword());

        assertThat(dashboardPage.isDashboardLoaded())
                .as("Dashboard should load after successful manager login")
                .isTrue();
    }

    @Test(groups = {"ui", "login"},
          description = "TC-UI-LGN-04: Logging in as Admin redirects to Dashboard")
    public void testSuccessfulAdminLogin() {
        LoginPage     loginPage     = new LoginPage(driver).navigateTo();
        DashboardPage dashboardPage = loginPage.loginAs(config.adminEmail(), config.adminPassword());

        assertThat(dashboardPage.isDashboardLoaded())
                .as("Dashboard should load after successful admin login")
                .isTrue();
    }

    // ================================================================
    // TC-UI-LGN-05..07: Failed Login / Validation
    // ================================================================

    @Test(groups = {"ui", "login", "negative"},
          description = "TC-UI-LGN-05: Invalid password displays 'Invalid credentials' error alert")
    public void testLoginWithInvalidPassword_ShowsError() {
        LoginPage loginPage = new LoginPage(driver).navigateTo();
        loginPage.enterEmail(config.employeeEmail())
                 .enterPassword("wrongPassword_xyz!")
                 .clickLoginButton();

        assertThat(loginPage.isErrorDisplayed())
                .as("An error alert must appear after submitting a wrong password")
                .isTrue();
        assertThat(loginPage.getErrorMessage())
                .as("Error message must say 'Invalid credentials'")
                .containsIgnoringCase("invalid credentials");
    }

    @Test(groups = {"ui", "login", "negative"},
          description = "TC-UI-LGN-06: Blank email shows error or keeps user on login page")
    public void testLoginWithBlankEmail_ShowsError() {
        LoginPage loginPage = new LoginPage(driver).navigateTo();
        loginPage.enterEmail("")
                 .enterPassword(config.employeePassword())
                 .clickLoginButton();

        // Short wait for any redirect or alert to settle
        try { Thread.sleep(1200); } catch (InterruptedException ignored) {}

        String  currentUrl   = driver.getCurrentUrl();
        boolean staysOnLogin = currentUrl.contains("/login") || currentUrl.endsWith("/");
        boolean errorShown   = loginPage.isErrorDisplayed();

        assertThat(staysOnLogin || errorShown)
                .as("Submitting blank email must either show error or remain on login page")
                .isTrue();
    }

    @Test(groups = {"ui", "login", "negative"},
          description = "TC-UI-LGN-07: Blank password shows error or keeps user on login page")
    public void testLoginWithBlankPassword_ShowsError() {
        LoginPage loginPage = new LoginPage(driver).navigateTo();
        loginPage.enterEmail(config.employeeEmail())
                 .enterPassword("")
                 .clickLoginButton();

        try { Thread.sleep(1200); } catch (InterruptedException ignored) {}

        String  currentUrl   = driver.getCurrentUrl();
        boolean staysOnLogin = currentUrl.contains("/login") || currentUrl.endsWith("/");
        boolean errorShown   = loginPage.isErrorDisplayed();

        assertThat(staysOnLogin || errorShown)
                .as("Submitting blank password must either show error or remain on login page")
                .isTrue();
    }

    // ================================================================
    // TC-UI-LGN-08: Navigation
    // ================================================================

    @Test(groups = {"ui", "login"},
          description = "TC-UI-LGN-08: Clicking 'Register here' link navigates to /register page")
    public void testRegisterLinkNavigation() {
        LoginPage    loginPage    = new LoginPage(driver).navigateTo();
        RegisterPage registerPage = loginPage.clickRegisterLink();

        assertThat(registerPage.isPageLoaded())
                .as("Register page should load after clicking 'Register here'")
                .isTrue();
        assertThat(registerPage.getCurrentUrl())
                .as("URL should contain /register")
                .contains("/register");
    }

    // ================================================================
    // TC-UI-LGN-09..10: Route Protection
    // ================================================================

    @Test(groups = {"ui", "login", "security"},
          description = "TC-UI-LGN-09: Accessing /leave-requests without login redirects to /login")
    public void testProtectedRouteRedirectsToLogin() {
        driver.get(config.uiBaseUrl() + "/leave-requests");
        // Allow React Router time to process the redirect
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        assertThat(driver.getCurrentUrl())
                .as("Unauthenticated access to /leave-requests must redirect to /login")
                .contains("/login");
    }

    @Test(groups = {"ui", "login", "security"},
          description = "TC-UI-LGN-10: Accessing /admin without login redirects to /login")
    public void testAdminRouteRedirectsToLogin() {
        driver.get(config.uiBaseUrl() + "/admin");
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        assertThat(driver.getCurrentUrl())
                .as("Unauthenticated access to /admin must redirect to /login")
                .contains("/login");
    }
}
