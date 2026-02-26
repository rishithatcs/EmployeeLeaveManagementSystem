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
 * API Tests for Authentication endpoints:
 *   POST /api/auth/login
 *   POST /api/auth/signup
 *   POST /api/auth/register
 *
 * These tests are self-contained — they do NOT depend on pre-acquired tokens
 * from @BeforeSuite because they ARE the tests that verify login itself.
 */
public class AuthApiTest extends BaseApiTest {

    @BeforeClass(alwaysRun = true)
    public void verifyServerReachable() {
        // Probe the server with a known-failing login so we can detect connectivity.
        // A 401 means the server IS up (it responded). A connection-refused means it's down.
        try {
            Response probe = ApiUtils.login("probe@probe.com", "probe");
            // If we reach here, server is up (even if 401). Good.
            log.info("AuthApiTest: server is reachable (probe returned HTTP {}).", probe.statusCode());
        } catch (Exception e) {
            throw new SkipException(
                "Backend server is not reachable at " + config.apiBaseUrl() +
                " — skipping Auth API tests. Start the backend and re-run. Detail: " + e.getMessage());
        }
    }

    // ================================================================
    // POST /api/auth/login — Happy Path
    // ================================================================

    @Test(groups = {"smoke", "api", "auth"},
          description = "TC-AUTH-01: Successful login with valid employee credentials returns JWT token")
    public void testLoginWithValidEmployeeCredentials() {
        Response response = ApiUtils.login(config.employeeEmail(), config.employeePassword());

        assertThat(response.statusCode())
                .as("HTTP status should be 200 OK for valid employee login")
                .isEqualTo(200);
        assertThat(response.jsonPath().getString("token"))
                .as("JWT token must be present and non-blank")
                .isNotBlank();
        assertThat(response.jsonPath().getString("email"))
                .as("Response email must match the login email")
                .isEqualTo(config.employeeEmail());
        assertThat(response.jsonPath().getList("roles"))
                .as("Roles list must contain EMPLOYEE")
                .contains("EMPLOYEE");
    }

    @Test(groups = {"smoke", "api", "auth"},
          description = "TC-AUTH-02: Successful login with valid manager credentials returns JWT token")
    public void testLoginWithValidManagerCredentials() {
        Response response = ApiUtils.login(config.managerEmail(), config.managerPassword());

        assertThat(response.statusCode())
                .as("HTTP status should be 200 OK for valid manager login")
                .isEqualTo(200);
        assertThat(response.jsonPath().getString("token")).isNotBlank();
        assertThat(response.jsonPath().getList("roles")).contains("MANAGER");
    }

    @Test(groups = {"smoke", "api", "auth"},
          description = "TC-AUTH-03: Successful login with valid admin credentials returns JWT token")
    public void testLoginWithValidAdminCredentials() {
        Response response = ApiUtils.login(config.adminEmail(), config.adminPassword());

        assertThat(response.statusCode())
                .as("HTTP status should be 200 OK for valid admin login")
                .isEqualTo(200);
        assertThat(response.jsonPath().getString("token")).isNotBlank();
        assertThat(response.jsonPath().getList("roles")).contains("ADMIN");
    }

    // ================================================================
    // POST /api/auth/login — Negative Cases
    // ================================================================

    @Test(groups = {"api", "auth", "negative"},
          description = "TC-AUTH-04: Login with invalid password returns 401 Unauthorized")
    public void testLoginWithInvalidPassword() {
        Response response = ApiUtils.login(config.employeeEmail(), "wrongPassword_xyz!");

        assertThat(response.statusCode())
                .as("Wrong password should return HTTP 401")
                .isEqualTo(401);
    }

    @Test(groups = {"api", "auth", "negative"},
          description = "TC-AUTH-05: Login with non-existent email returns 401 Unauthorized")
    public void testLoginWithNonExistentEmail() {
        Response response = ApiUtils.login("nobody_" + UUID.randomUUID() + "@elms.com", "anyPassword");

        assertThat(response.statusCode())
                .as("Non-existent user should return HTTP 401")
                .isEqualTo(401);
    }

    @Test(groups = {"api", "auth", "negative"},
          description = "TC-AUTH-06: Login with blank email returns non-200 response")
    public void testLoginWithBlankEmail() {
        Response response = ApiUtils.login("", config.employeePassword());

        assertThat(response.statusCode())
                .as("Blank email must not return 200")
                .isNotEqualTo(200);
    }

    @Test(groups = {"api", "auth", "negative"},
          description = "TC-AUTH-07: Login with blank password returns non-200 response")
    public void testLoginWithBlankPassword() {
        Response response = ApiUtils.login(config.employeeEmail(), "");

        assertThat(response.statusCode())
                .as("Blank password must not return 200")
                .isNotEqualTo(200);
    }

    // ================================================================
    // POST /api/auth/login — Response Structure
    // ================================================================

    @Test(groups = {"api", "auth"},
          description = "TC-AUTH-08: JWT response body contains all required fields (token, id, name, email, roles)")
    public void testLoginResponseContainsRequiredFields() {
        Response response = ApiUtils.login(config.employeeEmail(), config.employeePassword());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("token")).isNotBlank();
        assertThat(response.jsonPath().getLong("id")).isGreaterThan(0);
        assertThat(response.jsonPath().getString("name")).isNotBlank();
        assertThat(response.jsonPath().getString("email")).isNotBlank();
        assertThat(response.jsonPath().getList("roles")).isNotEmpty();
    }

    @Test(groups = {"api", "auth"},
          description = "TC-AUTH-13: Content-Type of login response is application/json")
    public void testLoginResponseContentType() {
        Response response = ApiUtils.login(config.employeeEmail(), config.employeePassword());

        assertThat(response.contentType())
                .as("Content-Type must include application/json")
                .contains("application/json");
    }

    // ================================================================
    // POST /api/auth/signup  &  /api/auth/register
    // ================================================================

    @Test(groups = {"api", "auth"},
          description = "TC-AUTH-09: Signup with valid employee data returns 200 and user DTO")
    public void testSignupWithValidEmployeeData() {
        String uniqueEmail = "emp_" + UUID.randomUUID().toString().substring(0, 8) + "@elms.com";
        Response response  = ApiUtils.register("New Employee", uniqueEmail, "Pass@1234", "EMPLOYEE");

        assertThat(response.statusCode())
                .as("Signup should return 200")
                .isEqualTo(200);
        assertThat(response.jsonPath().getString("email"))
                .isEqualTo(uniqueEmail);
        assertThat(response.jsonPath().getList("roles"))
                .contains("EMPLOYEE");
    }

    @Test(groups = {"api", "auth"},
          description = "TC-AUTH-10: Signup with valid manager data returns 200 with MANAGER role")
    public void testSignupWithValidManagerData() {
        String uniqueEmail = "mgr_" + UUID.randomUUID().toString().substring(0, 8) + "@elms.com";
        Response response  = ApiUtils.register("New Manager", uniqueEmail, "Pass@1234", "MANAGER");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("roles")).contains("MANAGER");
    }

    @Test(groups = {"api", "auth", "negative"},
          description = "TC-AUTH-11: Signup with a duplicate email returns a non-2xx error")
    public void testSignupWithDuplicateEmail() {
        // First registration — must succeed
        String uniqueEmail = "dup_" + UUID.randomUUID().toString().substring(0, 8) + "@elms.com";
        Response first = ApiUtils.register("Dup User", uniqueEmail, "Pass@1234", "EMPLOYEE");
        assertThat(first.statusCode()).as("First registration should succeed").isEqualTo(200);

        // Second registration with the SAME email — must fail
        Response duplicate = ApiUtils.register("Dup User Again", uniqueEmail, "Pass@1234", "EMPLOYEE");
        assertThat(duplicate.statusCode())
                .as("Duplicate email should not return 200")
                .isNotEqualTo(200);
    }

    @Test(groups = {"api", "auth"},
          description = "TC-AUTH-12: /register endpoint behaves identically to /signup")
    public void testRegisterEndpointBehavesLikeSignup() {
        String uniqueEmail = "reg_" + UUID.randomUUID().toString().substring(0, 8) + "@elms.com";
        Response response  = ApiUtils.baseSpec()
                .body(java.util.Map.of(
                        "name",     "Register Test",
                        "email",    uniqueEmail,
                        "password", "Pass@1234",
                        "roles",    new String[]{"EMPLOYEE"}
                ))
                .post("/auth/register");

        assertThat(response.statusCode())
                .as("/auth/register should return 200 just like /auth/signup")
                .isEqualTo(200);
        assertThat(response.jsonPath().getString("email")).isEqualTo(uniqueEmail);
    }
}
