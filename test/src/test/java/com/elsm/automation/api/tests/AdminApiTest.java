package com.elsm.automation.api.tests;

import com.elsm.automation.config.BaseApiTest;
import com.elsm.automation.utils.ApiUtils;
import io.restassured.response.Response;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * API Tests for Admin endpoints:
 *   POST   /api/admin/role
 *   DELETE /api/admin/role
 *   POST   /api/admin/leave-policy
 *   POST   /api/admin/adjust-balance
 *   GET    /api/admin/audit
 *   GET    /api/admin/report?type=...
 */
public class AdminApiTest extends BaseApiTest {

    private String testUserEmail;

    @BeforeClass(alwaysRun = true)
    public void classSetup() {
        // Acquire tokens independently of @BeforeSuite for robustness.
        try {
            adminToken    = ApiUtils.loginAndGetToken(config.adminEmail(),    config.adminPassword());
            employeeToken = ApiUtils.loginAndGetToken(config.employeeEmail(), config.employeePassword());
            log.info("AdminApiTest: tokens refreshed.");
        } catch (Exception e) {
            throw new SkipException(
                "Backend unavailable — cannot acquire tokens for Admin API tests. " +
                "Ensure the Spring Boot backend is running. Detail: " + e.getMessage());
        }

        // Register a dedicated test user for role-assignment / revoke tests.
        testUserEmail = "role_" + UUID.randomUUID().toString().substring(0, 8) + "@elms.com";
        try {
            Response reg = ApiUtils.register("Role Test User", testUserEmail, "Pass@1234", "EMPLOYEE");
            assertThat(reg.statusCode())
                    .as("Test user registration should succeed (HTTP 200)")
                    .isEqualTo(200);
            log.info("AdminApiTest: test user created — {}", testUserEmail);
        } catch (Exception e) {
            log.warn("AdminApiTest: test user creation failed — role tests may fail. {}", e.getMessage());
        }
    }

    // ================================================================
    // POST /api/admin/role  — Assign Role
    // ================================================================

    @Test(groups = {"smoke", "api", "admin"},
          description = "TC-ADM-01: Admin can assign a role to a user (200)")
    public void testAssignRole_Valid() {
        Response response = ApiUtils.assignRole(testUserEmail, "MANAGER", adminToken);

        assertThat(response.statusCode())
                .as("Assign role must return HTTP 200")
                .isEqualTo(200);
    }

    @Test(groups = {"api", "admin", "security"},
          description = "TC-ADM-02: Employee token cannot assign roles (403)")
    public void testAssignRole_WithEmployeeToken_ReturnsForbidden() {
        Response response = ApiUtils.assignRole(testUserEmail, "MANAGER", employeeToken);

        assertThat(response.statusCode())
                .as("Employee must not assign roles — expect 403")
                .isEqualTo(403);
    }

    @Test(groups = {"api", "admin", "negative"},
          description = "TC-ADM-03: Assigning role to non-existent user returns 4xx")
    public void testAssignRole_NonExistentUser_ReturnsError() {
        Response response = ApiUtils.assignRole("nobody_" + UUID.randomUUID() + "@elms.com", "EMPLOYEE", adminToken);

        assertThat(response.statusCode())
                .as("Non-existent user must return 4xx")
                .isBetween(400, 499);
    }

    // ================================================================
    // DELETE /api/admin/role  — Revoke Role
    // ================================================================

    @Test(groups = {"api", "admin"},
          dependsOnMethods = "testAssignRole_Valid",
          description = "TC-ADM-04: Admin can revoke a role from a user (200)")
    public void testRevokeRole_Valid() {
        Response response = ApiUtils.revokeRole(testUserEmail, "MANAGER", adminToken);

        assertThat(response.statusCode())
                .as("Revoke role must return HTTP 200")
                .isEqualTo(200);
    }

    @Test(groups = {"api", "admin", "security"},
          description = "TC-ADM-05: Employee token cannot revoke roles (403)")
    public void testRevokeRole_WithEmployeeToken_ReturnsForbidden() {
        Response response = ApiUtils.revokeRole(testUserEmail, "EMPLOYEE", employeeToken);

        assertThat(response.statusCode())
                .as("Employee must not revoke roles — expect 403")
                .isEqualTo(403);
    }

    // ================================================================
    // POST /api/admin/leave-policy
    // ================================================================

    @Test(groups = {"api", "admin"},
          description = "TC-ADM-06: Admin can update a leave policy (200)")
    public void testUpdateLeavePolicy_Valid() {
        Response response = ApiUtils.updateLeavePolicy("SICK", 15, adminToken);

        assertThat(response.statusCode())
                .as("Leave policy update must return HTTP 200")
                .isEqualTo(200);
    }

    @Test(groups = {"api", "admin", "security"},
          description = "TC-ADM-07: Employee token cannot update leave policies (403)")
    public void testUpdateLeavePolicy_WithEmployeeToken_ReturnsForbidden() {
        Response response = ApiUtils.updateLeavePolicy("SICK", 15, employeeToken);

        assertThat(response.statusCode())
                .as("Employee must not update leave policies — expect 403")
                .isEqualTo(403);
    }

    // ================================================================
    // POST /api/admin/adjust-balance
    // ================================================================

    @Test(groups = {"api", "admin"},
          description = "TC-ADM-08: Admin can adjust leave balance for an employee (200)")
    public void testAdjustLeaveBalance_Valid() {
        Response response = ApiUtils.adjustLeaveBalance(
                config.employeeEmail(), "VACATION", 5.0, adminToken);

        assertThat(response.statusCode())
                .as("Adjust balance must return HTTP 200")
                .isEqualTo(200);
    }

    @Test(groups = {"api", "admin", "negative"},
          description = "TC-ADM-09: Adjusting balance for non-existent user returns 400")
    public void testAdjustLeaveBalance_NonExistentUser_ReturnsBadRequest() {
        Response response = ApiUtils.adjustLeaveBalance(
                "ghost_" + UUID.randomUUID() + "@elms.com", "VACATION", 5.0, adminToken);

        assertThat(response.statusCode())
                .as("Non-existent user adjust-balance must return 400")
                .isEqualTo(400);
    }

    @Test(groups = {"api", "admin", "security"},
          description = "TC-ADM-10: Employee token cannot adjust leave balance (403)")
    public void testAdjustLeaveBalance_WithEmployeeToken_ReturnsForbidden() {
        Response response = ApiUtils.adjustLeaveBalance(
                config.employeeEmail(), "VACATION", 3.0, employeeToken);

        assertThat(response.statusCode())
                .as("Employee must not adjust leave balances — expect 403")
                .isEqualTo(403);
    }

    // ================================================================
    // GET /api/admin/audit
    // ================================================================

    @Test(groups = {"smoke", "api", "admin"},
          description = "TC-ADM-11: Admin can retrieve audit trails (200 with list)")
    public void testGetAuditTrails_ReturnsOk() {
        Response response = ApiUtils.getAuditTrails(adminToken);

        assertThat(response.statusCode())
                .as("Audit trail fetch must return HTTP 200")
                .isEqualTo(200);
        assertThat(response.jsonPath().getList("$"))
                .as("Audit trail response must be a list")
                .isNotNull();
    }

    @Test(groups = {"api", "admin", "security"},
          description = "TC-ADM-12: Employee token cannot access audit trails (403)")
    public void testGetAuditTrails_WithEmployeeToken_ReturnsForbidden() {
        Response response = ApiUtils.getAuditTrails(employeeToken);

        assertThat(response.statusCode())
                .as("Employee must not access audit trails — expect 403")
                .isEqualTo(403);
    }

    // ================================================================
    // GET /api/admin/report?type=...
    // ================================================================

    @Test(groups = {"api", "admin"},
          description = "TC-ADM-13: Admin can generate a leave report (200)")
    public void testGenerateReport_Valid() {
        Response response = ApiUtils.generateReport("monthly", adminToken);

        assertThat(response.statusCode())
                .as("Report generation must return HTTP 200")
                .isEqualTo(200);
    }

    @Test(groups = {"api", "admin", "security"},
          description = "TC-ADM-14: Employee token cannot generate reports (403)")
    public void testGenerateReport_WithEmployeeToken_ReturnsForbidden() {
        Response response = ApiUtils.generateReport("monthly", employeeToken);

        assertThat(response.statusCode())
                .as("Employee must not generate reports — expect 403")
                .isEqualTo(403);
    }

    // ================================================================
    // Cross-cutting: Unauthenticated access to all admin endpoints
    // ================================================================

    @Test(groups = {"api", "admin", "security"},
          description = "TC-ADM-15: All admin endpoints reject unauthenticated requests (401 or 403)")
    public void testAdminEndpoints_WithoutToken_ReturnsForbidden() {
        int auditStatus  = ApiUtils.baseSpec().get("/admin/audit").statusCode();
        int reportStatus = ApiUtils.baseSpec().queryParam("type", "monthly").get("/admin/report").statusCode();

        assertThat(auditStatus)
                .as("Unauthenticated /admin/audit must return 401 or 403")
                .isIn(401, 403);
        assertThat(reportStatus)
                .as("Unauthenticated /admin/report must return 401 or 403")
                .isIn(401, 403);
    }
}
