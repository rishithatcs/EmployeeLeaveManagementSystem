package com.elsm.automation.api.tests;

import com.elsm.automation.config.BaseApiTest;
import com.elsm.automation.utils.ApiUtils;
import io.restassured.response.Response;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * API Tests for Manager endpoints:
 *   GET  /api/manager/pending-requests
 *   POST /api/manager/decide-leave/{id}
 *   GET  /api/manager/team-leaves
 *   GET  /api/manager/team-history
 */
public class ManagerApiTest extends BaseApiTest {

    private long pendingLeaveId;

    @BeforeClass(alwaysRun = true)
    public void classSetup() {
        // Re-acquire tokens independently — robust against @BeforeSuite failure.
        try {
            managerToken  = ApiUtils.loginAndGetToken(config.managerEmail(),  config.managerPassword());
            employeeToken = ApiUtils.loginAndGetToken(config.employeeEmail(), config.employeePassword());
            adminToken    = ApiUtils.loginAndGetToken(config.adminEmail(),    config.adminPassword());
            log.info("ManagerApiTest: tokens refreshed.");
        } catch (Exception e) {
            throw new SkipException(
                "Backend unavailable — cannot acquire tokens for Manager API tests. " +
                "Ensure the Spring Boot backend is running. Detail: " + e.getMessage());
        }

        // Submit a fresh leave request so there is always at least one PENDING item.
        try {
            Response leaveResp = ApiUtils.submitLeaveRequest(
                    "VACATION", "2025-08-11", "2025-08-15", "Manager test leave", employeeToken);
            if (leaveResp.statusCode() == 200) {
                pendingLeaveId = leaveResp.jsonPath().getLong("id");
                log.info("ManagerApiTest: created leave request id={} for manager tests.", pendingLeaveId);
            } else {
                // Fall back to the request seeded by data.sql (id=1)
                pendingLeaveId = 1L;
                log.warn("ManagerApiTest: could not create fresh leave request (HTTP {}); " +
                         "falling back to id=1.", leaveResp.statusCode());
            }
        } catch (Exception e) {
            pendingLeaveId = 1L;
            log.warn("ManagerApiTest: leave request creation failed; falling back to id=1. {}", e.getMessage());
        }
    }

    // ================================================================
    // GET /api/manager/pending-requests
    // ================================================================

    @Test(groups = {"smoke", "api", "manager"},
          description = "TC-MGR-01: Manager can retrieve pending leave requests (200)")
    public void testGetPendingRequests_ReturnsOk() {
        Response response = ApiUtils.getPendingRequests(managerToken);

        assertThat(response.statusCode())
                .as("Pending requests endpoint must return HTTP 200")
                .isEqualTo(200);
        assertThat(response.jsonPath().getList("$"))
                .as("Response must be a list")
                .isNotNull();
    }

    @Test(groups = {"api", "manager"},
          description = "TC-MGR-02: Pending requests list items contain required fields")
    public void testPendingRequestsContainRequiredFields() {
        Response response = ApiUtils.getPendingRequests(managerToken);
        assertThat(response.statusCode()).isEqualTo(200);

        List<?> items = response.jsonPath().getList("$");
        if (!items.isEmpty()) {
            assertThat(response.jsonPath().getLong("[0].id")).isGreaterThan(0);
            assertThat(response.jsonPath().getString("[0].leaveType")).isNotBlank();
            assertThat(response.jsonPath().getString("[0].status")).isEqualTo("PENDING");
            assertThat(response.jsonPath().getString("[0].startDate")).isNotBlank();
            assertThat(response.jsonPath().getString("[0].endDate")).isNotBlank();
        }
    }

    @Test(groups = {"api", "manager", "security"},
          description = "TC-MGR-03: Accessing pending-requests without token returns 401 or 403")
    public void testGetPendingRequests_WithoutToken_ReturnsForbidden() {
        Response response = ApiUtils.baseSpec().get("/manager/pending-requests");
        assertThat(response.statusCode()).isIn(401, 403);
    }

    @Test(groups = {"api", "manager", "security"},
          description = "TC-MGR-04: Employee token cannot access manager pending-requests (403)")
    public void testGetPendingRequests_WithEmployeeToken_ReturnsForbidden() {
        Response response = ApiUtils.getPendingRequests(employeeToken);

        assertThat(response.statusCode())
                .as("Employee must not access /manager endpoints — expect 403")
                .isEqualTo(403);
    }

    // ================================================================
    // POST /api/manager/decide-leave/{id}
    // ================================================================

    @Test(groups = {"smoke", "api", "manager"},
          description = "TC-MGR-05: Manager can approve a pending leave request (200)")
    public void testApproveLeaveRequest_Valid() {
        Response response = ApiUtils.decideLeave(pendingLeaveId, true, "Approved by automation", managerToken);

        assertThat(response.statusCode())
                .as("Approve decision should return HTTP 200")
                .isEqualTo(200);
        String body = response.asString();
        assertThat(body)
                .as("Response must indicate success or APPROVED")
                .containsAnyOf("success", "APPROVED", "true");
    }

    @Test(groups = {"api", "manager"},
          description = "TC-MGR-06: Manager can reject a leave request (200)")
    public void testRejectLeaveRequest_Valid() {
        // Create a fresh leave request specifically for rejection
        Response leaveResp = ApiUtils.submitLeaveRequest(
                "SICK", "2025-10-06", "2025-10-08", "For rejection test", employeeToken);
        assertThat(leaveResp.statusCode())
                .as("Fresh leave request must be created successfully before rejection test")
                .isEqualTo(200);
        long rejectId = leaveResp.jsonPath().getLong("id");

        Response response = ApiUtils.decideLeave(rejectId, false, "Rejected by automation", managerToken);

        assertThat(response.statusCode())
                .as("Reject decision should return HTTP 200")
                .isEqualTo(200);
        String body = response.asString();
        assertThat(body)
                .as("Response must indicate REJECTED or false")
                .containsAnyOf("success", "REJECTED", "false");
    }

    @Test(groups = {"api", "manager", "negative"},
          description = "TC-MGR-07: Deciding on a non-existent leave request returns 4xx")
    public void testDecideLeave_NonExistentId_ReturnsError() {
        Response response = ApiUtils.decideLeave(999999L, true, "No comment", managerToken);

        assertThat(response.statusCode())
                .as("Non-existent leave ID must return 4xx error")
                .isBetween(400, 499);
    }

    @Test(groups = {"api", "manager", "security"},
          description = "TC-MGR-08: Employee token cannot approve/reject leaves (403)")
    public void testDecideLeave_WithEmployeeToken_ReturnsForbidden() {
        Response response = ApiUtils.decideLeave(pendingLeaveId, true, "Should fail", employeeToken);

        assertThat(response.statusCode())
                .as("Employee must not be able to decide leaves — expect 403")
                .isEqualTo(403);
    }

    // ================================================================
    // GET /api/manager/team-leaves
    // ================================================================

    @Test(groups = {"api", "manager"},
          description = "TC-MGR-09: Manager can retrieve team leave status (200)")
    public void testGetTeamLeaves_ReturnsOk() {
        Response response = ApiUtils.getTeamLeaves(managerToken);

        assertThat(response.statusCode())
                .as("Team leaves must return HTTP 200")
                .isEqualTo(200);
        assertThat(response.jsonPath().getList("$")).isNotNull();
    }

    @Test(groups = {"api", "manager", "security"},
          description = "TC-MGR-10: Employee token cannot access team-leaves (403)")
    public void testGetTeamLeaves_WithEmployeeToken_ReturnsForbidden() {
        Response response = ApiUtils.getTeamLeaves(employeeToken);
        assertThat(response.statusCode()).isEqualTo(403);
    }

    // ================================================================
    // GET /api/manager/team-history
    // ================================================================

    @Test(groups = {"api", "manager"},
          description = "TC-MGR-11: Manager can retrieve team leave history (200)")
    public void testGetTeamHistory_ReturnsOk() {
        Response response = ApiUtils.getTeamHistory(managerToken);

        assertThat(response.statusCode())
                .as("Team history must return HTTP 200")
                .isEqualTo(200);
        assertThat(response.jsonPath().getList("$")).isNotNull();
    }

    @Test(groups = {"api", "manager", "security"},
          description = "TC-MGR-12: Admin token cannot access manager-only team-history (403)")
    public void testGetTeamHistory_WithAdminToken_ReturnsForbidden() {
        Response response = ApiUtils.getTeamHistory(adminToken);

        assertThat(response.statusCode())
                .as("Admin must not access /manager endpoints — expect 403")
                .isEqualTo(403);
    }
}
