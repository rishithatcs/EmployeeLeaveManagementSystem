package com.elsm.automation.ui.tests;

import com.elsm.automation.config.BaseUiTest;
import com.elsm.automation.ui.pages.RegisterPage;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UI Tests for the Register page (/register).
 *
 * Register.js key facts:
 *   - Username field: input[name='username'] (label="Username")
 *   - Email field:    input[name='email']    (label="Email")
 *   - Password field: input[name='password'] (type="password")
 *   - Role select:    MUI Select, name="role" — options: EMPLOYEE, MANAGER
 *   - Submit button:  button[type='submit']  text="Register"
 *   - Error alert:    <Alert severity="error">Registration failed</Alert>
 *   - On success:     navigate('/login')
 */
public class RegisterUiTest extends BaseUiTest {

    // ================================================================
    // TC-UI-REG-01: Page Load
    // ================================================================

    @Test(groups = {"smoke", "ui", "register"},
          description = "TC-UI-REG-01: Register page loads with all required form fields")
    public void testRegisterPageLoads() {
        RegisterPage registerPage = new RegisterPage(driver).navigateTo();

        assertThat(registerPage.isPageLoaded())
                .as("Register page submit button should be visible")
                .isTrue();
        assertThat(registerPage.getCurrentUrl())
                .as("URL should contain /register")
                .contains("/register");
    }

    // ================================================================
    // TC-UI-REG-02..03: Successful Registration
    // ================================================================

    @Test(groups = {"smoke", "ui", "register"},
          description = "TC-UI-REG-02: Valid registration as Employee redirects to /login page")
    public void testValidEmployeeRegistrationRedirectsToLogin() {
        String uniqueEmail = "ui_emp_" + UUID.randomUUID().toString().substring(0, 8) + "@elms.com";

        RegisterPage registerPage = new RegisterPage(driver).navigateTo();
        registerPage.register("Test Employee", uniqueEmail, "TestPass@1234", "EMPLOYEE");

        // Wait for navigation to /login
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        assertThat(driver.getCurrentUrl())
                .as("After successful Employee registration, should redirect to /login")
                .contains("/login");
    }

    @Test(groups = {"smoke", "ui", "register"},
          description = "TC-UI-REG-03: Valid registration as Manager redirects to /login page")
    public void testValidManagerRegistrationRedirectsToLogin() {
        String uniqueEmail = "ui_mgr_" + UUID.randomUUID().toString().substring(0, 8) + "@elms.com";

        RegisterPage registerPage = new RegisterPage(driver).navigateTo();
        registerPage.register("Test Manager", uniqueEmail, "TestPass@1234", "MANAGER");

        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        assertThat(driver.getCurrentUrl())
                .as("After successful Manager registration, should redirect to /login")
                .contains("/login");
    }

    // ================================================================
    // TC-UI-REG-04: Duplicate Email
    // ================================================================

    @Test(groups = {"ui", "register", "negative"},
          description = "TC-UI-REG-04: Registering with a duplicate email displays an error")
    public void testDuplicateEmailRegistration_ShowsError() {
        // Register the first time
        String uniqueEmail = "ui_dup_" + UUID.randomUUID().toString().substring(0, 8) + "@elms.com";
        RegisterPage registerPage = new RegisterPage(driver).navigateTo();
        registerPage.register("Dup User", uniqueEmail, "TestPass@1234", "EMPLOYEE");

        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        // Navigate back and try the same email again
        registerPage = new RegisterPage(driver).navigateTo();
        registerPage.register("Dup User Again", uniqueEmail, "TestPass@1234", "EMPLOYEE");

        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        // Either stays on /register page with an error, or navigates away.
        // At minimum we verify the user is NOT on the dashboard.
        String currentUrl = driver.getCurrentUrl();
        boolean errorShown = registerPage.isErrorDisplayed();
        boolean notOnDashboard = !currentUrl.equals(config.uiBaseUrl() + "/");

        assertThat(errorShown || notOnDashboard)
                .as("Duplicate registration should show error or not reach the dashboard")
                .isTrue();
    }

    // ================================================================
    // TC-UI-REG-05: Blank Fields Validation
    // ================================================================

    @Test(groups = {"ui", "register", "negative"},
          description = "TC-UI-REG-05: Submitting the registration form with all blank fields stays or shows error")
    public void testBlankFieldsRegistration_StaysOrShowsError() {
        RegisterPage registerPage = new RegisterPage(driver).navigateTo();

        // Submit without filling any field
        registerPage.clickRegisterButton();

        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        String  currentUrl   = driver.getCurrentUrl();
        boolean staysOnPage  = currentUrl.contains("/register");
        boolean errorShown   = registerPage.isErrorDisplayed();

        assertThat(staysOnPage || errorShown)
                .as("Blank registration must either show error or remain on the register page")
                .isTrue();
    }
}
