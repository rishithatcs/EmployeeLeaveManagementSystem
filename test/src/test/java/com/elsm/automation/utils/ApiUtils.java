package com.elsm.automation.utils;

import com.elsm.automation.config.ConfigReader;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * Centralised REST Assured utility class.
 * Provides pre-built request specs and helper methods for the ELMS API.
 */
public class ApiUtils {

    private static final Logger log = LogManager.getLogger(ApiUtils.class);
    private static final ConfigReader config = ConfigReader.getInstance();

    private ApiUtils() {}

    // ---------------------------------------------------------------
    // Request Specifications
    // ---------------------------------------------------------------

    /** Unauthenticated base spec. */
    public static RequestSpecification baseSpec() {
        return RestAssured
                .given()
                .baseUri(config.apiBaseUrl())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .log().ifValidationFails();
    }

    /** Authenticated base spec using a Bearer JWT. */
    public static RequestSpecification authSpec(String jwtToken) {
        return baseSpec().header("Authorization", "Bearer " + jwtToken);
    }

    // ---------------------------------------------------------------
    // Auth helpers
    // ---------------------------------------------------------------

    /**
     * Performs a login request and returns the full response.
     */
    public static Response login(String email, String password) {
        log.info("POST /auth/login  email={}", email);
        return baseSpec()
                .body(Map.of("email", email, "password", password))
                .post("/auth/login");
    }

    /**
     * Convenience: logs in and extracts the JWT token string.
     * Throws if login fails.
     */
    public static String loginAndGetToken(String email, String password) {
        Response resp = login(email, password);
        if (resp.statusCode() != 200) {
            throw new RuntimeException("Login failed for " + email +
                    " – HTTP " + resp.statusCode() + " – " + resp.asString());
        }
        String token = resp.jsonPath().getString("token");
        log.info("JWT acquired for {}", email);
        return token;
    }

    /**
     * Registers a new user and returns the response.
     */
    public static Response register(String name, String email, String password, String role) {
        log.info("POST /auth/signup  email={} role={}", email, role);
        return baseSpec()
                .body(Map.of(
                        "name",     name,
                        "email",    email,
                        "password", password,
                        "roles",    new String[]{role}
                ))
                .post("/auth/signup");
    }

    // ---------------------------------------------------------------
    // Employee helpers
    // ---------------------------------------------------------------

    public static Response getLeaveBalance(String token) {
        log.info("GET /employee/leave-balance");
        return authSpec(token).get("/employee/leave-balance");
    }

    public static Response getLeaveHistory(String token) {
        log.info("GET /employee/leave-history");
        return authSpec(token).get("/employee/leave-history");
    }

    public static Response submitLeaveRequest(String leaveType, String startDate,
                                               String endDate, String notes,
                                               String token) {
        log.info("POST /employee/leave-request  type={} {} → {}", leaveType, startDate, endDate);
        return authSpec(token)
                .body(Map.of(
                        "leaveType", leaveType,
                        "startDate", startDate,
                        "endDate",   endDate,
                        "notes",     notes
                ))
                .post("/employee/leave-request");
    }

    public static Response modifyLeaveRequest(long id, String startDate,
                                               String endDate, String notes,
                                               String token) {
        log.info("PUT /employee/leave-request/{}  {} → {}", id, startDate, endDate);
        return authSpec(token)
                .body(Map.of(
                        "startDate", startDate,
                        "endDate",   endDate,
                        "notes",     notes
                ))
                .put("/employee/leave-request/" + id);
    }

    public static Response cancelLeaveRequest(long id, String token) {
        log.info("DELETE /employee/leave-request/{}", id);
        return authSpec(token).delete("/employee/leave-request/" + id);
    }

    // ---------------------------------------------------------------
    // Manager helpers
    // ---------------------------------------------------------------

    public static Response getPendingRequests(String token) {
        log.info("GET /manager/pending-requests");
        return authSpec(token).get("/manager/pending-requests");
    }

    public static Response decideLeave(long id, boolean approved, String comments, String token) {
        log.info("POST /manager/decide-leave/{}  approved={}", id, approved);
        return authSpec(token)
                .body(Map.of("approved", approved, "comments", comments))
                .post("/manager/decide-leave/" + id);
    }

    public static Response getTeamLeaves(String token) {
        log.info("GET /manager/team-leaves");
        return authSpec(token).get("/manager/team-leaves");
    }

    public static Response getTeamHistory(String token) {
        log.info("GET /manager/team-history");
        return authSpec(token).get("/manager/team-history");
    }

    // ---------------------------------------------------------------
    // Admin helpers
    // ---------------------------------------------------------------

    public static Response assignRole(String userEmail, String role, String token) {
        log.info("POST /admin/role  email={} role={}", userEmail, role);
        return authSpec(token)
                .body(Map.of("userEmail", userEmail, "role", role))
                .post("/admin/role");
    }

    public static Response revokeRole(String userEmail, String role, String token) {
        log.info("DELETE /admin/role  email={} role={}", userEmail, role);
        return authSpec(token)
                .body(Map.of("userEmail", userEmail, "role", role))
                .delete("/admin/role");
    }

    public static Response updateLeavePolicy(String leaveType, int maxDays, String token) {
        log.info("POST /admin/leave-policy  type={} maxDays={}", leaveType, maxDays);
        return authSpec(token)
                .body(Map.of("leaveType", leaveType, "maxDays", maxDays))
                .post("/admin/leave-policy");
    }

    public static Response adjustLeaveBalance(String userEmail, String leaveType,
                                               double adjustment, String token) {
        log.info("POST /admin/adjust-balance  email={} type={} adj={}", userEmail, leaveType, adjustment);
        return authSpec(token)
                .body(Map.of(
                        "userEmail",   userEmail,
                        "leaveType",   leaveType,
                        "adjustment",  adjustment
                ))
                .post("/admin/adjust-balance");
    }

    public static Response getAuditTrails(String token) {
        log.info("GET /admin/audit");
        return authSpec(token).get("/admin/audit");
    }

    public static Response generateReport(String type, String token) {
        log.info("GET /admin/report?type={}", type);
        return authSpec(token).queryParam("type", type).get("/admin/report");
    }
}
