package com.elsm.automation.config;

import com.elsm.automation.utils.ApiUtils;
import com.elsm.automation.utils.ExtentReportManager;
import com.elsm.automation.utils.TestListener;
import io.restassured.RestAssured;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;

/**
 * Base class for all REST Assured API tests.
 *
 * KEY DESIGN DECISION:
 *   @BeforeSuite must NEVER throw an exception.
 *   If it throws, TestNG marks the entire suite configuration as FAILED
 *   and skips ALL tests — including unrelated UI tests in the same XML suite.
 *
 *   We catch all errors here and store null tokens. Individual test classes
 *   re-acquire their own tokens in @BeforeClass so they get a clear assertion
 *   failure rather than a silent skip.
 */
@Listeners(TestListener.class)
public abstract class BaseApiTest {

    protected static final Logger log = LogManager.getLogger(BaseApiTest.class);
    protected static final ConfigReader config = ConfigReader.getInstance();

    // Shared JWT tokens — populated in @BeforeSuite; may be null if server is down.
    protected static String employeeToken;
    protected static String managerToken;
    protected static String adminToken;

    @BeforeSuite(alwaysRun = true)
    public void globalSetup() {
        RestAssured.baseURI = config.apiBaseUrl();
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        log.info("REST Assured base URI set to: {}", config.apiBaseUrl());

        // ── Acquire tokens, but NEVER throw ──────────────────────────────────
        // If the server is down or credentials are wrong we log a warning and
        // leave the tokens as null.  Each API test class refreshes its own
        // tokens in @BeforeClass so failures are clear and isolated.
        employeeToken = safeLogin(config.employeeEmail(), config.employeePassword());
        managerToken  = safeLogin(config.managerEmail(),  config.managerPassword());
        adminToken    = safeLogin(config.adminEmail(),    config.adminPassword());

        if (employeeToken != null && managerToken != null && adminToken != null) {
            log.info("JWT tokens acquired for employee / manager / admin.");
        } else {
            log.warn("One or more tokens could not be acquired in @BeforeSuite. " +
                     "Individual test classes will attempt their own login.");
        }
    }

    @AfterSuite(alwaysRun = true)
    public void globalTearDown() {
        ExtentReportManager.flush();
        log.info("Report flushed.");
    }

    /**
     * Safe login — returns the JWT token on success, or null on any error.
     * Never throws.
     */
    protected static String safeLogin(String email, String password) {
        try {
            return ApiUtils.loginAndGetToken(email, password);
        } catch (Exception e) {
            log.warn("Could not acquire token for {} in @BeforeSuite: {}", email, e.getMessage());
            return null;
        }
    }
}
