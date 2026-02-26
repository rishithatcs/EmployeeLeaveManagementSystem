package com.elsm.automation.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.util.Properties;

/**
 * Singleton configuration reader.
 * Loads values from config/config.properties on first access.
 */
public class ConfigReader {

    private static final Logger log = LogManager.getLogger(ConfigReader.class);
    private static final String CONFIG_FILE = "config/config.properties";
    private static ConfigReader instance;
    private final Properties props = new Properties();

    private ConfigReader() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (is == null) {
                throw new RuntimeException("Cannot find " + CONFIG_FILE + " on the classpath.");
            }
            props.load(is);
            log.info("Configuration loaded from {}", CONFIG_FILE);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load configuration", e);
        }
    }

    public static ConfigReader getInstance() {
        if (instance == null) {
            synchronized (ConfigReader.class) {
                if (instance == null) {
                    instance = new ConfigReader();
                }
            }
        }
        return instance;
    }

    public String get(String key) {
        // Allow JVM system-property override (e.g. -Dapi.base.url=...)
        String sysProp = System.getProperty(key);
        return sysProp != null ? sysProp : props.getProperty(key);
    }

    public String get(String key, String defaultValue) {
        String val = get(key);
        return (val != null && !val.isBlank()) ? val : defaultValue;
    }

    public int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(get(key));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String val = get(key);
        return val != null ? Boolean.parseBoolean(val) : defaultValue;
    }

    // ---- Convenience accessors ----

    public String apiBaseUrl()         { return get("api.base.url"); }
    public String uiBaseUrl()          { return get("ui.base.url"); }
    public String browser()            { return get("browser", "chrome"); }
    public boolean headless()          { return getBoolean("headless", false); }
    public int implicitWait()          { return getInt("implicit.wait.seconds", 10); }
    public int explicitWait()          { return getInt("explicit.wait.seconds", 15); }
    public int pageLoadTimeout()       { return getInt("page.load.timeout.seconds", 30); }

    public String employeeEmail()      { return get("employee.email"); }
    public String employeePassword()   { return get("employee.password"); }
    public String managerEmail()       { return get("manager.email"); }
    public String managerPassword()    { return get("manager.password"); }
    public String adminEmail()         { return get("admin.email"); }
    public String adminPassword()      { return get("admin.password"); }

    public String newUserName()        { return get("new.user.name"); }
    public String newUserEmail()       { return get("new.user.email"); }
    public String newUserPassword()    { return get("new.user.password"); }
    public String newUserRole()        { return get("new.user.role"); }

    public String reportOutputDir()    { return get("report.output.dir", "target/test-reports"); }
    public String reportName()         { return get("report.name", "ELMS Automation Report"); }
}
