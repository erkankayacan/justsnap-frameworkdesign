# FRAMEWORK_GUIDE.md

## 1. Add New Test Cases
- Create under `src/test/java/com/example/...` following package structure (`ui`, `api`, `db`).
- Annotate with TestNG: `@Test`, `@BeforeMethod`, `@AfterMethod` as needed.
- Use data providers for dataset-driven scenarios.
- For API tests, centralize base URI and common headers in `ApiBase`.

## 2. Page Object Guidelines
- Keep pages **behavioral** (actions + assertions), not just locators.
- Prefer explicit waits; wrap in helper methods.
- Avoid static sleeps; use `ExpectedConditions` patterns.
- Do not embed flaky timing assumptions; centralize timeouts.

## 3. Naming Conventions
- Packages: `com.example.ui`, `com.example.api`, `com.example.db`.
- Classes: `*Page`, `*Api`, `*Tests`.
- Methods: `action_shouldExpectedResult()` for tests.
- Locators: `By` fields `btnLogin`, `txtEmail`, etc.

## 4. Code Review Checklist
- Readability & small diffs.
- Page Objects free of test logic.
- No hardcoded waits or credentials.
- Deterministic assertions (no timing races).
- Clean dependency versions; no duplicates.
- Unit/API/UI boundaries respected.

## 5. Best Practices
- DRY utilities for waits, drivers, data.
- Test data builders over raw maps.
- Isolate side effects; clean up in `@AfterMethod`.
- Prefer **idempotent** tests; parallel-safe where possible.
