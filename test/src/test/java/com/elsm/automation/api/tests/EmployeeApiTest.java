package com.elsm.automation.api.tests;

import com.elsm.automation.config.BaseApiTest;
import com.elsm.automation.utils.ApiUtils;
import io.restassured.response.Response;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * API Tests for Employee endpoints:
 *   GET    /api/employee/leave-balance
 *   GET    /api/employee/leave-history
 *   POST   /api/employee/leave-request
 *   PUT    /api/employee/leave-request/{id}
 *   DELETE /api/employee/leave-request/{id}
 *
 * @BeforeClass acquires fresh tokens independently from @BeforeSuite,
 * so these tests run correctly even if @BeforeSuite failed.
 */
public class EmployeeApiTest extends BaseApiTest {

    private long createdLeaveRequestId;

    @BeforeClass(alwaysRun = true)
    public void classSetup() {
        // Always re-acquire employee token here so this class is self-contained.
        try {
            employeeToken = ApiUtils.loginAndGetToken(config.employeeEmail(), config.employeePassword());
            managerToken  = ApiUtils.loginAndGetToken(config.managerEmail(),  config.managerPassword());
            log.info("EmployeeApiTest: tokens refreshed — employee & manager.");
        } catch (Exception e) {
            throw new SkipException(
                "Backend unavailable — cannot acquire tokens for Employee API tests. " +
                "Ensure the Spring Boot backend is running. Detail: " + e.getMessage());
        }
    }

    // ================================================================
    // GET /api/employee/leave-balance
    // ================================================================

    @Test(groups = {"smoke", "api", "employee"},
          description = "TC-EMP-01: Employee can fetch their leave balance (200 with list)")
    public void testGetLeaveBalance_ReturnsOk() {
        Response response = ApiUtils.getLeaveBalance(employeeToken);

        assertThat(response.statusCode())
                .as("Leave balance fetch should return HTTP 200")
                .isEqualTo(200);
        assertThat(response.jsonPath().getList("$"))
                .as("Response body should be a non-empty list of balances")
                .isNotEmpty();
    }

    @Test(groups = {"api", "employee"},
          description = "TC-EMP-02: Leave balance response items contain leaveType and balance fields")
    public void testLeaveBalanceContainsRequiredFields() {
        Response response = ApiUtils.getLeaveBalance(employeeToken);
        assertThat(response.statusCode()).isEqualTo(200);

        // data.sql seeds 4 leave-balance rows for the employee
        assertThat(response.jsonPath().getString("[0].leaveType"))
                .as("leaveType field must be present")
                .isNotNull();
        assertThat(response.jsonPath().getFloat("[0].balance"))
                .as("balance field must be >= 0")
                .isGreaterThanOrEqualTo(0);
    }

    @Test(groups = {"api", "employee", "security"},
          description = "TC-EMP-03: Accessing leave-balance without a token returns 401 or 403")
    public void testGetLeaveBalance_WithoutToken_ReturnsForbidden() {
        Response response = ApiUtils.baseSpec().get("/employee/leave-balance");

        assertThat(response.statusCode())
                .as("Unauthenticated request must be rejected with 401 or 403")
                .isIn(401, 403);
    }

    @Test(groups = {"api", "employee", "security"},
          description = "TC-EMP-04: Manager token cannot access employee leave-balance (403)")
    public void testGetLeaveBalance_WithManagerToken_ReturnsForbidden() {
        Response response = ApiUtils.getLeaveBalance(managerToken);

        assertThat(response.statusCode())
                .as("Manager token must not access /employee endpoints — expect 403")
                .isEqualTo(403);
    }

    // ================================================================
    // GET /api/employee/leave-history
    // ================================================================

    @Test(groups = {"api", "employee"},
          description = "TC-EMP-05: Employee can fetch their leave history (200)")
    public void testGetLeaveHistory_ReturnsOk() {
        Response response = ApiUtils.getLeaveHistory(employeeToken);

        assertThat(response.statusCode())
                .as("Leave history should return HTTP 200")
                .isEqualTo(200);
        assertThat(response.jsonPath().getList("$"))
                .as("Response must be a list (may be empty for fresh DB)")
                .isNotNull();
    }

    @Test(groups = {"api", "employee"},
          description = "TC-EMP-06: Leave history items contain id, leaveType, status, startDate, endDate")
    public void testLeaveHistoryContainsRequiredFields() {
        Response response = ApiUtils.getLeaveHistory(employeeToken);
        assertThat(response.statusCode()).isEqualTo(200);

        // data.sql seeds one VACATION request for the employee
        java.util.List<?> history = response.jsonPath().getList("$");
        if (!history.isEmpty()) {
            assertThat(response.jsonPath().getLong("[0].id")).isGreaterThan(0);
            assertThat(response.jsonPath().getString("[0].leaveType")).isNotBlank();
            assertThat(response.jsonPath().getString("[0].status")).isNotBlank();
            assertThat(response.jsonPath().getString("[0].startDate")).isNotBlank();
            assertThat(response.jsonPath().getString("[0].endDate")).isNotBlank();
        }
    }

    // ================================================================
    // POST /api/employee/leave-request
    // ================================================================

    @Test(groups = {"smoke", "api", "employee"},
          description = "TC-EMP-07: Employee can submit a valid leave request (200 with PENDING status)")
    public void testSubmitLeaveRequest_Valid() {
        Response response = ApiUtils.submitLeaveRequest(
                "SICK", "2025-09-01", "2025-09-03", "Flu", employeeToken);

        assertThat(response.statusCode())
                .as("Valid leave request must return HTTP 200")
                .isEqualTo(200);
        assertThat(response.jsonPath().getString("status"))
                .as("Newly submitted request status must be PENDING")
                .isEqualTo("PENDING");
        assertThat(response.jsonPath().getString("leaveType"))
                .as("Leave type must be echoed back as SICK")
                .isEqualTo("SICK");

        // Save ID for the dependent modify and cancel tests
        createdLeaveRequestId = response.jsonPath().getLong("id");
        log.info("Created leave request id={} for subsequent tests.", createdLeaveRequestId);
    }

    @Test(groups = {"api", "employee", "negative"},
          description = "TC-EMP-08: Submitting a leave request without leaveType returns 400")
    public void testSubmitLeaveRequest_MissingLeaveType_ReturnsBadRequest() {
        Response response = ApiUtils.authSpec(employeeToken)
                .body(java.util.Map.of(
                        "startDate", "2025-10-01",
                        "endDate",   "2025-10-03",
                        "notes",     "Missing type"
                ))
                .post("/employee/leave-request");

        assertThat(response.statusCode())
                .as("Missing leaveType must return HTTP 400")
                .isEqualTo(400);
    }

    @Test(groups = {"api", "employee", "negative"},
          description = "TC-EMP-09: Submitting a leave request without startDate returns 400")
    public void testSubmitLeaveRequest_MissingStartDate_ReturnsBadRequest() {
        Response response = ApiUtils.authSpec(employeeToken)
                .body(java.util.Map.of(
                        "leaveType", "VACATION",
                        "endDate",   "2025-10-03",
                        "notes",     "Missing start date"
                ))
                .post("/employee/leave-request");

        assertThat(response.statusCode())
                .as("Missing startDate must return HTTP 400")
                .isEqualTo(400);
    }

    @Test(groups = {"api", "employee", "negative"},
          description = "TC-EMP-10: Submitting a leave request without endDate returns 400")
    public void testSubmitLeaveRequest_MissingEndDate_ReturnsBadRequest() {
        Response response = ApiUtils.authSpec(employeeToken)
                .body(java.util.Map.of(
                        "leaveType", "VACATION",
                        "startDate", "2025-10-01",
                        "notes",     "Missing end date"
                ))
                .post("/employee/leave-request");

        assertThat(response.statusCode())
                .as("Missing endDate must return HTTP 400")
                .isEqualTo(400);
    }

    @Test(groups = {"api", "employee", "security"},
          description = "TC-EMP-11: Submitting leave request without token returns 401 or 403")
    public void testSubmitLeaveRequest_WithoutToken_ReturnsForbidden() {
        Response response = ApiUtils.baseSpec()
                .body(java.util.Map.of(
                        "leaveType", "SICK",
                        "startDate", "2025-11-01",
                        "endDate",   "2025-11-02",
                        "notes",     "No auth"
                ))
                .post("/employee/leave-request");

        assertThat(response.statusCode())
                .as("No token must return 401 or 403")
                .isIn(401, 403);
    }

    // ================================================================
    // PUT /api/employee/leave-request/{id}
    // ================================================================

    @Test(groups = {"api", "employee"},
          dependsOnMethods = "testSubmitLeaveRequest_Valid",
          description = "TC-EMP-12: Employee can modify a PENDING leave request (200)")
    public void testModifyLeaveRequest_Valid() {
        Response response = ApiUtils.modifyLeaveRequest(
                createdLeaveRequestId,
                "2025-09-05", "2025-09-07", "Updated reason",
                employeeToken);

        assertThat(response.statusCode())
                .as("Modify leave request should return HTTP 200")
                .isEqualTo(200);
        assertThat(response.jsonPath().getString("startDate"))
                .as("startDate must reflect the update")
                .isEqualTo("2025-09-05");
    }

    // ================================================================
    // DELETE /api/employee/leave-request/{id}
    // ================================================================

    @Test(groups = {"api", "employee"},
          dependsOnMethods = {"testSubmitLeaveRequest_Valid", "testModifyLeaveRequest_Valid"},
          description = "TC-EMP-13: Employee can cancel a PENDING leave request (200 with CANCELLED status)")
    public void testCancelLeaveRequest_Valid() {
        Response response = ApiUtils.cancelLeaveRequest(createdLeaveRequestId, employeeToken);

        assertThat(response.statusCode())
                .as("Cancel leave request should return HTTP 200")
                .isEqualTo(200);
        assertThat(response.jsonPath().getString("status"))
                .as("Cancelled request must have status CANCELLED")
                .isEqualTo("CANCELLED");
    }

    @Test(groups = {"api", "employee", "negative"},
          description = "TC-EMP-14: Cancelling a non-existent leave request returns 4xx")
    public void testCancelLeaveRequest_NonExistentId_ReturnsError() {
        Response response = ApiUtils.cancelLeaveRequest(999999L, employeeToken);

        assertThat(response.statusCode())
                .as("Non-existent leave request ID must return a 4xx error")
                .isBetween(400, 499);
    }
}
