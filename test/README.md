# ELMS Test Automation Framework

**Employee Leave Management System — Complete Selenium + TestNG + REST Assured Automation Suite**

---

## 📁 Framework Structure

```
test/
├── pom.xml                                         ← Maven build file (all dependencies & profiles)
├── README.md                                       ← This file
└── src/
    └── test/
        ├── java/
        │   └── com/elsm/automation/
        │       ├── config/
        │       │   ├── ConfigReader.java           ← Singleton config loader (config.properties)
        │       │   ├── DriverManager.java          ← Thread-safe Selenium WebDriver factory
        │       │   ├── BaseApiTest.java            ← Base class for all API tests
        │       │   └── BaseUiTest.java             ← Base class for all UI tests
        │       │
        │       ├── utils/
        │       │   ├── ApiUtils.java               ← REST Assured helpers for all endpoints
        │       │   ├── ExtentReportManager.java    ← HTML Spark report manager
        │       │   └── TestListener.java           ← TestNG listener → ExtentReports bridge
        │       │
        │       ├── ui/
        │       │   ├── pages/                      ← Page Object Model (POM) classes
        │       │   │   ├── LoginPage.java
        │       │   │   ├── RegisterPage.java
        │       │   │   ├── DashboardPage.java
        │       │   │   ├── LeaveRequestsPage.java
        │       │   │   ├── ManagerDashboardPage.java
        │       │   │   └── AdminDashboardPage.java
        │       │   └── tests/                      ← Selenium / TestNG UI test classes
        │       │       ├── LoginUiTest.java
        │       │       ├── RegisterUiTest.java
        │       │       ├── DashboardUiTest.java
        │       │       ├── LeaveRequestsUiTest.java
        │       │       ├── ManagerDashboardUiTest.java
        │       │       └── AdminDashboardUiTest.java
        │       │
        │       └── api/
        │           └── tests/                      ← REST Assured API test classes
        │               ├── AuthApiTest.java
        │               ├── EmployeeApiTest.java
        │               ├── ManagerApiTest.java
        │               └── AdminApiTest.java
        │
        └── resources/
            ├── config/
            │   └── config.properties               ← Environment & user configuration
            ├── testng/
            │   ├── testng-full-suite.xml           ← All tests (default)
            │   ├── testng-api.xml                  ← API tests only
            │   ├── testng-ui.xml                   ← UI tests only
            │   └── testng-smoke.xml                ← Critical-path smoke tests
            ├── log4j2.xml                          ← Logging configuration
```

---

## 🧩 Technology Stack

| Technology            | Version   | Role                                  |
|-----------------------|-----------|---------------------------------------|
| Java                  | 21        | Language                              |
| Maven                 | 3.9+      | Build & dependency management         |
| TestNG                | 7.10.2    | Test orchestration & assertion runner |
| Selenium WebDriver    | 4.21.0    | Browser automation (UI tests)         |
| WebDriverManager      | 5.8.0     | Auto-manages browser driver binaries  |
| REST Assured          | 5.4.0     | HTTP API testing                      |
| AssertJ               | 3.25.3    | Fluent assertions                     |
| ExtentReports         | 5.1.1     | HTML test reports                     |
| Jackson               | 2.17.1    | JSON serialisation                    |
| Log4j2                | 2.23.1    | Structured logging                    |
| Lombok                | 1.18.32   | Boilerplate reduction                 |

---

## 🔧 Prerequisites

1. **Java 21** — `java -version`
2. **Maven 3.9+** — `mvn -version`
3. **Google Chrome** (or Firefox/Edge) — installed and up to date
4. **Backend running** — `http://localhost:8080` (Spring Boot on H2, seeded with `data.sql`)
5. **Frontend running** — `http://localhost:3000` (React, `npm start` inside `frontendCapStone/`)

---

## ⚙️ Configuration

Edit `src/test/resources/config/config.properties` to match your environment:

```properties
# API
api.base.url=http://localhost:8080/api

# Frontend
ui.base.url=http://localhost:3000

# Browser: chrome | firefox | edge
browser=chrome
headless=false          # Set to true for CI/CD pipelines

# Seed users (from data.sql — password hash for "12345")
employee.email=employee@test.com
employee.password=12345
manager.email=manager@test.com
manager.password=12345
admin.email=admin@test.com
admin.password=12345
```

You can **override any property** at runtime via JVM system properties:

```bash
mvn test -Dheadless=true -Dbrowser=firefox -Dapi.base.url=http://myserver:8080/api
```

---

## ▶️ Running Tests

### Run the full suite (API + UI)
```bash
cd test
mvn test
```

### Run only API tests (no browser needed)
```bash
mvn test -P api-tests
```

### Run only UI tests (browser required)
```bash
mvn test -P ui-tests
```

### Run smoke tests (critical-path only)
```bash
mvn test -P smoke
```

### Run headless (CI/CD mode)
```bash
mvn test -P api-tests -Dheadless=true
```

---

## 📊 Test Coverage Summary

### API Tests (REST Assured)

| Test Class          | # Tests | Endpoints Covered                                                              |
|---------------------|---------|--------------------------------------------------------------------------------|
| `AuthApiTest`       | 13      | `POST /auth/login`, `POST /auth/signup`, `POST /auth/register`                 |
| `EmployeeApiTest`   | 14      | `GET /employee/leave-balance`, `GET /employee/leave-history`, `POST/PUT/DELETE /employee/leave-request` |
| `ManagerApiTest`    | 12      | `GET /manager/pending-requests`, `POST /manager/decide-leave/{id}`, `GET /manager/team-leaves`, `GET /manager/team-history` |
| `AdminApiTest`      | 15      | `POST/DELETE /admin/role`, `POST /admin/leave-policy`, `POST /admin/adjust-balance`, `GET /admin/audit`, `GET /admin/report` |
| **Total**           | **54**  |                                                                                |

### UI Tests (Selenium + TestNG)

| Test Class               | # Tests | Pages Covered              |
|--------------------------|---------|----------------------------|
| `LoginUiTest`            | 10      | `/login`                   |
| `RegisterUiTest`         | 5       | `/register`                |
| `DashboardUiTest`        | 9       | `/` (Employee + Manager)   |
| `LeaveRequestsUiTest`    | 9       | `/leave-requests`          |
| `ManagerDashboardUiTest` | 6       | `/manager`                 |
| `AdminDashboardUiTest`   | 6       | `/admin`, `/audit`         |
| **Total**                | **45**  |                            |

### Grand Total: **99 automated test cases**

---

## 🧪 Test Groups / Tags

| Group       | Description                                    |
|-------------|------------------------------------------------|
| `smoke`     | Critical-path sanity checks (fast, minimal)    |
| `api`       | All REST Assured API tests                     |
| `ui`        | All Selenium UI tests                          |
| `auth`      | Authentication-specific tests                  |
| `employee`  | Employee role tests                            |
| `manager`   | Manager role tests                             |
| `admin`     | Admin role tests                               |
| `security`  | Role-based access control (RBAC) tests         |
| `negative`  | Negative / error-path tests                    |
| `login`     | Login page UI tests                            |
| `register`  | Register page UI tests                         |
| `dashboard` | Dashboard page UI tests                        |
| `leave`     | Leave Requests page UI tests                   |

---

## 📈 Reports

After test execution, HTML reports are generated in:

```
test/target/test-reports/ELMS_Report_<timestamp>.html
```

A log file is also available at:

```
test/target/test-reports/automation.log
```

Open the HTML report in any browser for a full breakdown of passed, failed, and skipped tests with step-level details.

---

## 🏗️ Design Patterns

| Pattern                         | Where Used                         |
|---------------------------------|------------------------------------|
| Page Object Model (POM)         | All `ui/pages/` classes            |
| Factory / ThreadLocal           | `DriverManager` (parallel-safe)    |
| Singleton                       | `ConfigReader`, `ExtentReportManager` |
| Base Test Class Inheritance     | `BaseApiTest`, `BaseUiTest`        |
| Builder-style fluent methods    | All Page Object actions            |
| TestNG Listener                 | `TestListener` → ExtentReports     |

---

## 🔒 Security Test Coverage

The framework extensively covers **role-based access control (RBAC)**:

- Every protected API endpoint is tested with **no token** (→ 401/403)
- Every employee endpoint is tested with a **manager token** (→ 403)
- Every manager endpoint is tested with an **employee token** (→ 403)
- Every admin endpoint is tested with an **employee token** (→ 403)
- Every protected UI route is tested without authentication (→ redirect to `/login`)
- Role-restricted UI pages are tested with unauthorised roles (→ redirect to `/`)

---

## 🤝 CI/CD Integration

Add to your pipeline (example GitHub Actions step):

```yaml
- name: Run ELMS Smoke Tests
  run: |
    cd test
    mvn test -P smoke -Dheadless=true -Dapi.base.url=${{ env.API_URL }}
```

---

## 📝 Notes

- The framework is **completely decoupled** from the production code.
- **No production files are modified** by this framework.
- Tests follow the **AAA pattern** (Arrange, Act, Assert).
- All test methods have a `description` attribute that becomes the ExtentReport test name.
- `dependsOnMethods` is used only where test ordering is strictly required (e.g., modify before cancel).
