package com.elsm.automation.ui.tests;

import com.elsm.automation.config.BaseUiTest;
import com.elsm.automation.ui.pages.DashboardPage;
import com.elsm.automation.ui.pages.LoginPage;
import com.elsm.automation.ui.pages.LeaveRequestsPage;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UI Tests for the Dashboard page (/).
 *
 * Dashboard.js key facts:
 *   - Welcome heading:      <Typography variant="h4">Welcome, ...</Typography>
 *   - Logout button:        <Button>Logout</Button>
 *   - Leave Balance section: <Typography variant="h6">Leave Balance</Typography>
 *   - Leave History section: <Typography variant="h6">Leave History</Typography>
 *   - Apply for Leave btn:  <Button>Apply for Leave</Button>  (EMPLOYEE only)
 *   - Pending Requests section: <Typography variant="h5">Pending Leave Requests</Typography> (MANAGER only)
 *
 * Each test logs in fresh (new browser per @BeforeMethod) so they are independent.
 */
public class DashboardUiTest extends BaseUiTest {

    // ── Helper: login and return the DashboardPage ─────────────────

    private DashboardPage loginAs(String email, String password) {
        return new LoginPage(driver).navigateTo().loginAs(email, password);
    }

    // ================================================================
    // TC-UI-DSH-01: Welcome message
    // ================================================================

    @Test(groups = {"smoke", "ui", "dashboard"},
          description = "TC-UI-DSH-01: Employee dashboard shows 'Welcome' heading after login")
    public void testEmployeeDashboardShowsWelcomeMessage() {
        DashboardPage dashboard = loginAs(config.employeeEmail(), config.employeePassword());

        assertThat(dashboard.isDashboardLoaded())
                .as("Dashboard must load after employee login")
                .isTrue();
        assertThat(dashboard.getWelcomeText())
                .as("Welcome heading must contain the word 'Welcome'")
                .containsIgnoringCase("Welcome");
    }

    // ================================================================
    // TC-UI-DSH-02: Leave Balance section
    // ================================================================

    @Test(groups = {"ui", "dashboard"},
          description = "TC-UI-DSH-02: Employee dashboard displays Leave Balance section")
    public void testEmployeeDashboardShowsLeaveBalanceSection() {
        DashboardPage dashboard = loginAs(config.employeeEmail(), config.employeePassword());

        assertThat(dashboard.isDashboardLoaded()).isTrue();
        assertThat(dashboard.isLeaveBalanceSectionVisible())
                .as("Leave Balance section must be visible on the employee dashboard")
                .isTrue();
    }

    // ================================================================
    // TC-UI-DSH-03: Leave History section
    // ================================================================

    @Test(groups = {"ui", "dashboard"},
          description = "TC-UI-DSH-03: Employee dashboard displays Leave History section")
    public void testEmployeeDashboardShowsLeaveHistorySection() {
        DashboardPage dashboard = loginAs(config.employeeEmail(), config.employeePassword());

        assertThat(dashboard.isDashboardLoaded()).isTrue();
        assertThat(dashboard.isLeaveHistorySectionVisible())
                .as("Leave History section must be visible on the employee dashboard")
                .isTrue();
    }

    // ================================================================
    // TC-UI-DSH-04: Apply for Leave button (employee only)
    // ================================================================

    @Test(groups = {"smoke", "ui", "dashboard"},
          description = "TC-UI-DSH-04: Employee dashboard shows 'Apply for Leave' button")
    public void testEmployeeDashboardShowsApplyLeaveButton() {
        DashboardPage dashboard = loginAs(config.employeeEmail(), config.employeePassword());

        assertThat(dashboard.isDashboardLoaded()).isTrue();
        assertThat(dashboard.isApplyLeaveButtonVisible())
                .as("'Apply for Leave' button must be visible for EMPLOYEE role")
                .isTrue();
    }

    // ================================================================
    // TC-UI-DSH-05: Apply for Leave navigates to /leave-requests
    // ================================================================

    @Test(groups = {"ui", "dashboard"},
          description = "TC-UI-DSH-05: Clicking 'Apply for Leave' navigates to /leave-requests")
    public void testApplyLeaveButtonNavigatesToLeaveRequests() {
        DashboardPage      dashboard       = loginAs(config.employeeEmail(), config.employeePassword());
        LeaveRequestsPage  leaveReqPage    = dashboard.clickApplyForLeave();

        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        assertThat(leaveReqPage.getCurrentUrl())
                .as("Clicking 'Apply for Leave' must navigate to /leave-requests")
                .contains("/leave-requests");
    }

    // ================================================================
    // TC-UI-DSH-06: Manager dashboard shows Pending Requests section
    // ================================================================

    @Test(groups = {"ui", "dashboard"},
          description = "TC-UI-DSH-06: Manager dashboard shows Welcome message and Pending Leave Requests section")
    public void testManagerDashboardShowsPendingSection() {
        DashboardPage dashboard = loginAs(config.managerEmail(), config.managerPassword());

        assertThat(dashboard.isDashboardLoaded())
                .as("Dashboard must load after manager login")
                .isTrue();
        assertThat(dashboard.isPendingRequestsSectionVisible())
                .as("Pending Leave Requests section must appear on the MANAGER dashboard")
                .isTrue();
    }

    // ================================================================
    // TC-UI-DSH-07: Manager dashboard does NOT show Apply for Leave
    // ================================================================

    @Test(groups = {"ui", "dashboard"},
          description = "TC-UI-DSH-07: Manager dashboard does NOT show 'Apply for Leave' button")
    public void testManagerDashboardDoesNotShowApplyLeaveButton() {
        DashboardPage dashboard = loginAs(config.managerEmail(), config.managerPassword());

        assertThat(dashboard.isDashboardLoaded()).isTrue();
        assertThat(dashboard.isApplyLeaveButtonVisible())
                .as("'Apply for Leave' button must NOT be visible for MANAGER role")
                .isFalse();
    }

    // ================================================================
    // TC-UI-DSH-08: Logout redirects to /login
    // ================================================================

    @Test(groups = {"smoke", "ui", "dashboard"},
          description = "TC-UI-DSH-08: Clicking Logout redirects to /login page")
    public void testLogoutRedirectsToLogin() {
        DashboardPage dashboard = loginAs(config.employeeEmail(), config.employeePassword());
        assertThat(dashboard.isDashboardLoaded()).isTrue();

        dashboard.clickLogout();

        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        assertThat(driver.getCurrentUrl())
                .as("After logout, user must be redirected to /login")
                .contains("/login");
    }

    // ================================================================
    // TC-UI-DSH-09: After logout, dashboard is protected
    // ================================================================

    @Test(groups = {"ui", "dashboard", "security"},
          description = "TC-UI-DSH-09: After logout, accessing / redirects back to /login")
    public void testAfterLogoutDashboardIsProtected() {
        // Login and then logout
        DashboardPage dashboard = loginAs(config.employeeEmail(), config.employeePassword());
        assertThat(dashboard.isDashboardLoaded()).isTrue();
        dashboard.clickLogout();

        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        // Now try to navigate back to the dashboard
        driver.get(config.uiBaseUrl() + "/");
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        assertThat(driver.getCurrentUrl())
                .as("After logout, navigating to / must redirect to /login")
                .contains("/login");
    }
}
