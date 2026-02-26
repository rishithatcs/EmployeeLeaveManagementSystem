package com.elsm.automation.ui.tests;

import com.elsm.automation.config.BaseUiTest;
import com.elsm.automation.ui.pages.DashboardPage;
import com.elsm.automation.ui.pages.LeaveRequestsPage;
import com.elsm.automation.ui.pages.LoginPage;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UI Tests for the Leave Requests page (/leave-requests).
 *
 * LeaveRequests.js key facts:
 *   - Heading:        "Your Leave Requests"
 *   - Apply button:   "Apply for Leave" → opens Modal
 *   - Modal fields:   leaveType (select), startDate (date), endDate (date), notes (textarea)
 *   - Submit:         "Submit" button → calls api.createLeaveRequest
 *   - Cancel:         "Cancel" button → closes modal without submitting
 *   - Validation:     if any field empty → setError('All fields required')
 *   - Success:        setSuccess('Leave request submitted!')
 */
public class LeaveRequestsUiTest extends BaseUiTest {

    /** Login as employee and navigate to the Leave Requests page. */
    private LeaveRequestsPage loginAndNavigate() {
        DashboardPage dashboard = new LoginPage(driver)
                .navigateTo()
                .loginAs(config.employeeEmail(), config.employeePassword());
        // Verify dashboard loaded
        dashboard.isDashboardLoaded();
        // Navigate to leave requests
        return new LeaveRequestsPage(driver).navigateTo();
    }

    // ================================================================
    // TC-UI-LVR-01: Page Load
    // ================================================================

    @Test(groups = {"smoke", "ui", "leave"},
          description = "TC-UI-LVR-01: Leave Requests page loads successfully after employee login")
    public void testLeaveRequestsPageLoads() {
        LeaveRequestsPage page = loginAndNavigate();

        assertThat(page.isPageLoaded())
                .as("Leave Requests page heading must be visible")
                .isTrue();
    }

    // ================================================================
    // TC-UI-LVR-02: Modal opens
    // ================================================================

    @Test(groups = {"smoke", "ui", "leave"},
          description = "TC-UI-LVR-02: Clicking 'Apply for Leave' button opens the modal form")
    public void testOpenModalShowsForm() {
        LeaveRequestsPage page = loginAndNavigate();
        page.clickApplyForLeave();

        assertThat(page.isModalVisible())
                .as("Modal form must become visible after clicking 'Apply for Leave'")
                .isTrue();
    }

    // ================================================================
    // TC-UI-LVR-03: Cancel closes modal without submitting
    // ================================================================

    @Test(groups = {"ui", "leave"},
          description = "TC-UI-LVR-03: Clicking 'Cancel' in the modal closes it without submitting")
    public void testCancelModalDoesNotSubmit() {
        LeaveRequestsPage page = loginAndNavigate();
        page.clickApplyForLeave();
        assertThat(page.isModalVisible()).isTrue();

        page.clickCancel();

        try { Thread.sleep(800); } catch (InterruptedException ignored) {}

        // After cancel, the modal's submit button should no longer be visible
        assertThat(page.isModalVisible())
                .as("Modal must close after clicking 'Cancel'")
                .isFalse();
    }

    // ================================================================
    // TC-UI-LVR-04: Valid SICK leave request submission
    // ================================================================

    @Test(groups = {"smoke", "ui", "leave"},
          description = "TC-UI-LVR-04: Submitting a valid SICK leave request shows success alert")
    public void testValidLeaveRequestSubmissionShowsSuccess() {
        LeaveRequestsPage page = loginAndNavigate();
        page.clickApplyForLeave();

        page.selectLeaveType("SICK")
            .enterStartDate("09/01/2025")
            .enterEndDate("09/03/2025")
            .enterNotes("Automation SICK test");
        page.clickSubmit();

        assertThat(page.isSuccessDisplayed())
                .as("A success alert must appear after a valid SICK leave request")
                .isTrue();
    }

    // ================================================================
    // TC-UI-LVR-05: Valid VACATION leave request submission
    // ================================================================

    @Test(groups = {"ui", "leave"},
          description = "TC-UI-LVR-05: Submitting a valid VACATION leave request shows success alert")
    public void testVacationLeaveRequestSubmissionShowsSuccess() {
        LeaveRequestsPage page = loginAndNavigate();
        page.clickApplyForLeave();

        page.selectLeaveType("VACATION")
            .enterStartDate("10/01/2025")
            .enterEndDate("10/05/2025")
            .enterNotes("Automation VACATION test");
        page.clickSubmit();

        assertThat(page.isSuccessDisplayed())
                .as("A success alert must appear after a valid VACATION leave request")
                .isTrue();
    }

    // ================================================================
    // TC-UI-LVR-06: Missing leave type
    // ================================================================

    @Test(groups = {"ui", "leave", "negative"},
          description = "TC-UI-LVR-06: Submitting without selecting a leave type shows validation error")
    public void testSubmitWithoutLeaveType_ShowsError() {
        LeaveRequestsPage page = loginAndNavigate();
        page.clickApplyForLeave();

        // Fill everything EXCEPT leave type
        page.enterStartDate("10/01/2025")
            .enterEndDate("10/03/2025")
            .enterNotes("Missing leave type");
        page.clickSubmit();

        assertThat(page.isErrorDisplayed())
                .as("An error alert must appear when leave type is missing")
                .isTrue();
    }

    // ================================================================
    // TC-UI-LVR-07: Missing notes/reason
    // ================================================================

    @Test(groups = {"ui", "leave", "negative"},
          description = "TC-UI-LVR-07: Submitting without a reason/notes shows validation error")
    public void testSubmitWithoutNotes_ShowsError() {
        LeaveRequestsPage page = loginAndNavigate();
        page.clickApplyForLeave();

        page.selectLeaveType("SICK")
            .enterStartDate("10/01/2025")
            .enterEndDate("10/03/2025");
        // intentionally skip enterNotes()
        page.clickSubmit();

        assertThat(page.isErrorDisplayed())
                .as("An error alert must appear when notes/reason is missing")
                .isTrue();
    }

    // ================================================================
    // TC-UI-LVR-08: All fields blank
    // ================================================================

    @Test(groups = {"ui", "leave", "negative"},
          description = "TC-UI-LVR-08: Submitting the form with all blank fields shows a validation error")
    public void testSubmitWithAllBlankFields_ShowsError() {
        LeaveRequestsPage page = loginAndNavigate();
        page.clickApplyForLeave();

        // Submit immediately without filling anything
        page.clickSubmit();

        assertThat(page.isErrorDisplayed())
                .as("Error alert must appear when all fields are blank")
                .isTrue();
    }

    // ================================================================
    // TC-UI-LVR-09: Unauthenticated redirect
    // ================================================================

    @Test(groups = {"ui", "leave", "security"},
          description = "TC-UI-LVR-09: Unauthenticated access to /leave-requests redirects to /login")
    public void testUnauthenticatedAccessRedirects() {
        // Navigate directly without logging in
        driver.get(config.uiBaseUrl() + "/leave-requests");
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        assertThat(driver.getCurrentUrl())
                .as("Unauthenticated access to /leave-requests must redirect to /login")
                .contains("/login");
    }
}
