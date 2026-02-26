package com.elsm.automation.ui.tests;

import com.elsm.automation.config.BaseUiTest;
import com.elsm.automation.ui.pages.AdminDashboardPage;
import com.elsm.automation.ui.pages.LoginPage;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UI Tests for the Admin Dashboard page (/admin).
 *
 * AdminDashboard.js key facts:
 *   - Heading: <h2>Admin Dashboard</h2>   (plain HTML, NOT MUI Typography)
 *
 * App.js routes:
 *   - /admin → PrivateRoute roles=['ADMIN']   — non-ADMIN redirected to /
 *   - /audit → PrivateRoute roles=['ADMIN']   — non-ADMIN redirected to /
 */
public class AdminDashboardUiTest extends BaseUiTest {

    /** Login as admin and navigate to /admin. */
    private AdminDashboardPage loginAsAdminAndNavigate() {
        new LoginPage(driver).navigateTo()
                .loginAs(config.adminEmail(), config.adminPassword())
                .isDashboardLoaded();
        return new AdminDashboardPage(driver).navigateTo();
    }

    // ================================================================
    // TC-UI-ADM-01: Admin can access /admin
    // ================================================================

    @Test(groups = {"smoke", "ui", "admin"},
          description = "TC-UI-ADM-01: Admin user can successfully access the Admin Dashboard")
    public void testAdminCanAccessDashboard() {
        AdminDashboardPage page = loginAsAdminAndNavigate();

        assertThat(page.isPageLoaded())
                .as("Admin Dashboard page must load for an ADMIN user")
                .isTrue();
        assertThat(page.getCurrentUrl())
                .as("URL must contain /admin")
                .contains("/admin");
    }

    // ================================================================
    // TC-UI-ADM-02: Heading reads "Admin Dashboard"
    // ================================================================

    @Test(groups = {"ui", "admin"},
          description = "TC-UI-ADM-02: Admin Dashboard page heading is 'Admin Dashboard'")
    public void testAdminDashboardHeading() {
        AdminDashboardPage page = loginAsAdminAndNavigate();

        assertThat(page.getHeadingText())
                .as("Heading must say 'Admin Dashboard'")
                .containsIgnoringCase("Admin Dashboard");
    }

    // ================================================================
    // TC-UI-ADM-03: Employee cannot access /admin (redirected to /)
    // ================================================================

    @Test(groups = {"ui", "admin", "security"},
          description = "TC-UI-ADM-03: Employee cannot access /admin page (redirected to /)")
    public void testEmployeeCannotAccessAdminDashboard() {
        // Login as employee
        new LoginPage(driver).navigateTo()
                .loginAs(config.employeeEmail(), config.employeePassword())
                .isDashboardLoaded();

        // Navigate to /admin
        driver.get(config.uiBaseUrl() + "/admin");
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        // PrivateRoute with roles=['ADMIN'] redirects to '/'
        String currentUrl = driver.getCurrentUrl();
        assertThat(currentUrl.endsWith("/") || currentUrl.contains("/login"))
                .as("Employee accessing /admin must be redirected to / or /login")
                .isTrue();
    }

    // ================================================================
    // TC-UI-ADM-04: Manager cannot access /admin (redirected to /)
    // ================================================================

    @Test(groups = {"ui", "admin", "security"},
          description = "TC-UI-ADM-04: Manager cannot access /admin page (redirected to /)")
    public void testManagerCannotAccessAdminDashboard() {
        new LoginPage(driver).navigateTo()
                .loginAs(config.managerEmail(), config.managerPassword())
                .isDashboardLoaded();

        driver.get(config.uiBaseUrl() + "/admin");
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        String currentUrl = driver.getCurrentUrl();
        assertThat(currentUrl.endsWith("/") || currentUrl.contains("/login"))
                .as("Manager accessing /admin must be redirected to / or /login")
                .isTrue();
    }

    // ================================================================
    // TC-UI-ADM-05: Unauthenticated access to /admin redirects to /login
    // ================================================================

    @Test(groups = {"ui", "admin", "security"},
          description = "TC-UI-ADM-05: Unauthenticated access to /admin redirects to /login")
    public void testUnauthenticatedAccessToAdminRedirects() {
        driver.get(config.uiBaseUrl() + "/admin");
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        assertThat(driver.getCurrentUrl())
                .as("Unauthenticated /admin must redirect to /login")
                .contains("/login");
    }

    // ================================================================
    // TC-UI-ADM-06: Unauthenticated access to /audit redirects to /login
    // ================================================================

    @Test(groups = {"ui", "admin", "security"},
          description = "TC-UI-ADM-06: Unauthenticated access to /audit redirects to /login")
    public void testUnauthenticatedAccessToAuditRedirects() {
        driver.get(config.uiBaseUrl() + "/audit");
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        assertThat(driver.getCurrentUrl())
                .as("Unauthenticated /audit must redirect to /login")
                .contains("/login");
    }
}
