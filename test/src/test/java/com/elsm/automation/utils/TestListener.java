package com.elsm.automation.utils;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * TestNG listener that hooks into the ExtentReports lifecycle and
 * logs every test start, pass, fail, and skip.
 */
public class TestListener implements ITestListener {

    private static final Logger log = LogManager.getLogger(TestListener.class);

    @Override
    public void onStart(ITestContext context) {
        log.info("======= TEST SUITE STARTED: {} =======", context.getName());
    }

    @Override
    public void onFinish(ITestContext context) {
        log.info("======= TEST SUITE FINISHED: {} =======", context.getName());
        ExtentReportManager.flush();
    }

    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getDescription();
        if (testName == null || testName.isBlank()) {
            testName = result.getMethod().getMethodName();
        }
        log.info("[START] {}", testName);
        ExtentTest test = ExtentReportManager.getInstance()
                .createTest(testName, result.getMethod().getDescription());
        test.assignCategory(result.getTestClass().getName()
                .replace("com.elsm.automation.", "")
                .split("\\.")[0].toUpperCase());
        ExtentReportManager.setTest(test);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        log.info("[PASS]  {}", result.getMethod().getMethodName());
        ExtentTest test = ExtentReportManager.getTest();
        if (test != null) test.log(Status.PASS, "Test passed.");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        log.error("[FAIL]  {} – {}", result.getMethod().getMethodName(),
                result.getThrowable().getMessage());
        ExtentTest test = ExtentReportManager.getTest();
        if (test != null) {
            test.log(Status.FAIL, "Test failed: " + result.getThrowable().getMessage());
            test.log(Status.FAIL, result.getThrowable());
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        log.warn("[SKIP]  {}", result.getMethod().getMethodName());
        ExtentTest test = ExtentReportManager.getTest();
        if (test != null) test.log(Status.SKIP, "Test skipped.");
    }
}
