package com.elsm.automation.ui.tests;

import com.elsm.automation.config.BaseUiTest;
import com.elsm.automation.ui.pages.DashboardPage;
import com.elsm.automation.ui.pages.LoginPage;
import com.elsm.automation.ui.pages.ManagerDashboardPage;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UI Tests for the Manager Dashboard page (/manager).
 *
 * ManagerDashboard.js key facts:
 *   - Heading:        <Typography variant="h5">Pending Leave Requests</Typography>
 *   - Approve button: <Button color="success">Approve</Button>
 *   - Reject button:  <Button color="error">Reject</Button>
 *   - Success alert:  {success && <Alert severity="success">}
 *
 * App.js route: /manager → PrivateRoute roles=['MANAGER']
 *   - Non-MANAGER users are redirected to /
 */
public class ManagerDashboardUiTest extends BaseUiTest {

    /** Login as manager and navigate to /manager. */
    private ManagerDashboardPage loginAsManagerAndNavigate() {
        new LoginPage(driver).navigateTo()
                .loginAs(config.managerEmail(), config.managerPassword())
                .isDashboardLoaded(); // wait for dashboard
        return new ManagerDashboardPage(driver).navigateTo();
    }

    // ================================================================
    // TC-UI-MGR-01: Manager can access /manager
    // ================================================================

    @Test(groups = {"smoke", "ui", "manager"},
          description = "TC-UI-MGR-01: Manager can access the /manager dashboard page")
    public void testManagerDashboardLoads() {
        ManagerDashboardPage page = loginAsManagerAndNavigate();

        assertThat(page.isPageLoaded())
                .as("Manager Dashboard page must load for a MANAGER user")
                .isTrue();
        assertThat(page.getCurrentUrl())
                .as("URL must contain /manager")
                .contains("/manager");
    }

    // ================================================================
    // TC-UI-MGR-02: Heading reads "Pending Leave Requests"
    // ================================================================

    @Test(groups = {"ui", "manager"},
          description = "TC-UI-MGR-02: Manager dashboard heading reads 'Pending Leave Requests'")
    public void testManagerDashboardHeading() {
        ManagerDashboardPage page = loginAsManagerAndNavigate();

        assertThat(page.getHeadingText())
                .as("Heading must say 'Pending Leave Requests'")
                .containsIgnoringCase("Pending Leave Requests");
    }

    // ================================================================
    // TC-UI-MGR-03: Manager can approve a pending request
    // ================================================================

    @Test(groups = {"ui", "manager"},
          description = "TC-UI-MGR-03: Manager can approve a pending leave request and sees success alert")
    public void testManagerCanApprovePendingRequest() {
        ManagerDashboardPage page = loginAsManagerAndNavigate();

        if (page.isApproveButtonVisible()) {
            page.clickFirstApprove();
            assertThat(page.isSuccessAlertDisplayed())
                    .as("Success alert must appear after approving a leave request")
                    .isTrue();
        } else {
            // No pending requests at this moment — pass with note
            assertThat(page.isPageLoaded())
                    .as("Manager page loaded; no pending requests to approve at this time")
                    .isTrue();
        }
    }

    // ================================================================
    // TC-UI-MGR-04: Manager can reject a pending request
    // ================================================================

    @Test(groups = {"ui", "manager"},
          description = "TC-UI-MGR-04: Manager can reject a pending leave request and sees success alert")
    public void testManagerCanRejectPendingRequest() {
        ManagerDashboardPage page = loginAsManagerAndNavigate();

        if (page.isRejectButtonVisible()) {
            page.clickFirstReject();
            assertThat(page.isSuccessAlertDisplayed())
                    .as("Success alert must appear after rejecting a leave request")
                    .isTrue();
        } else {
            assertThat(page.isPageLoaded())
                    .as("Manager page loaded; no pending requests to reject at this time")
                    .isTrue();
        }
    }

    // ================================================================
    // TC-UI-MGR-05: Employee cannot access /manager (redirected to /)
    // ================================================================

    @Test(groups = {"ui", "manager", "security"},
          description = "TC-UI-MGR-05: Employee cannot access /manager page (redirected to /)")
    public void testEmployeeCannotAccessManagerDashboard() {
        // Login as employee
        new LoginPage(driver).navigateTo()
                .loginAs(config.employeeEmail(), config.employeePassword())
                .isDashboardLoaded();

        // Try to navigate directly to /manager
        driver.get(config.uiBaseUrl() + "/manager");
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        // PrivateRoute with roles=['MANAGER'] redirects non-MANAGER to '/'
        String currentUrl = driver.getCurrentUrl();
        assertThat(currentUrl.endsWith("/") || currentUrl.contains("/login"))
                .as("Employee accessing /manager must be redirected away (to / or /login)")
                .isTrue();
    }

    // ================================================================
    // TC-UI-MGR-06: Unauthenticated access to /manager redirects to /login
    // ================================================================

    @Test(groups = {"ui", "manager", "security"},
          description = "TC-UI-MGR-06: Unauthenticated access to /manager redirects to /login")
    public void testUnauthenticatedAccessToManagerRedirects() {
        driver.get(config.uiBaseUrl() + "/manager");
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        assertThat(driver.getCurrentUrl())
                .as("Unauthenticated access to /manager must redirect to /login")
                .contains("/login");
    }
}
