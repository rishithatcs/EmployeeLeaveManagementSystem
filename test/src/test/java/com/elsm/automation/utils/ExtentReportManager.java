package com.elsm.automation.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.elsm.automation.config.ConfigReader;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Thread-safe ExtentReports manager.
 * Creates an HTML Spark report under target/test-reports/.
 */
public class ExtentReportManager {

    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> testThreadLocal = new ThreadLocal<>();
    private static final ConfigReader config = ConfigReader.getInstance();

    private ExtentReportManager() {}

    public static synchronized ExtentReports getInstance() {
        if (extent == null) {
            String outputDir   = config.reportOutputDir();
            String timestamp   = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String reportPath  = outputDir + File.separator + "ELMS_Report_" + timestamp + ".html";

            new File(outputDir).mkdirs();

            ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
            spark.config().setDocumentTitle(config.reportName());
            spark.config().setReportName(config.reportName());
            spark.config().setTheme(Theme.STANDARD);
            spark.config().setEncoding("UTF-8");

            extent = new ExtentReports();
            extent.attachReporter(spark);
            extent.setSystemInfo("Application", "Employee Leave Management System");
            extent.setSystemInfo("Environment", "Test");
            extent.setSystemInfo("Browser", config.browser());
            extent.setSystemInfo("Base URL", config.apiBaseUrl());
        }
        return extent;
    }

    public static ExtentTest getTest() {
        return testThreadLocal.get();
    }

    public static void setTest(ExtentTest test) {
        testThreadLocal.set(test);
    }

    public static synchronized void flush() {
        if (extent != null) {
            extent.flush();
        }
    }

    public static void removeTest() {
        testThreadLocal.remove();
    }
}
