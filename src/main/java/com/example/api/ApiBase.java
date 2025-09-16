package com.example.api;

import com.example.utils.Config;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.config.ConnectionConfig;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.LogConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.time.Duration;
import java.util.Map;
import java.util.function.UnaryOperator;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;

/**
 * Base class for API tests.
 *
 * Features:
 * - Base URI from Config.apiBaseUrl() with safe default
 * - Default JSON content-type and accept headers
 * - Optional bearer token / API key helpers
 * - Central logging toggle via -Dapi.log=true
 * - Timeouts from Config.timeout() (or override with -Dapi.timeout=5s)
 * - Convenience request() mutator + HTTP verb helpers (get/post/put/patch/delete)
 * - Relaxed HTTPS (self-signed friendly for test envs)
 */
public abstract class ApiBase {

    /** Shared request / response specs for subclasses. */
    protected RequestSpecification req;
    protected ResponseSpecification resp2xx;

    /** If you want to pin a basePath for a suite, set here (e.g. "/v1"). */
    protected String basePath = "";

    /** Cached bearer token (optional). */
    protected String bearerToken;

    /** Cached API-Key header value (optional). */
    protected String apiKey;


    @AfterClass(alwaysRun = true)
    public void tearDownApi() {
        // If you override global RestAssured state per suite, clear here if needed.
        // (Usually not required; left for completeness.)
    }

    /* ---------------------------------------------------
     * Convenience: auth / headers / mutators
     * --------------------------------------------------- */

    /** Set a bearer token for Authorization header in subsequent requests. */
    protected void setBearerToken(String token) {
        this.bearerToken = token;
        this.req = withHeader("Authorization", "Bearer " + token);
    }

    /** Clear bearer token from default spec. */
    protected void clearBearerToken() {
        this.bearerToken = null;
        this.req = removeHeader("Authorization");
    }

    /** Set an API key header (customize name if needed). */
    protected void setApiKey(String key, String headerName) {
        this.apiKey = key;
        this.req = withHeader(headerName, key);
    }

    /** Add or override a header in the default request spec. */
    protected RequestSpecification withHeader(String name, String value) {
        return mutateSpec(rs -> rs.header(name, value));
    }

    /** Remove a header from the default request spec (best-effort). */
    protected RequestSpecification removeHeader(String name) {
        return mutateSpec(rs -> {
            if (rs instanceof FilterableRequestSpecification frs) {
                frs.removeHeader(name);
            }
            return rs;
        });
    }

    /** Add multiple headers. */
    protected RequestSpecification withHeaders(Map<String, ?> headers) {
        return mutateSpec(rs -> rs.headers(headers));
    }
    @BeforeClass
    public void setupApi() {
        // Base URI comes from config (with safe default)
        RestAssured.baseURI = Config.apiBaseUrl();

        // Optional: set basePath if your API uses versioning (e.g., /v1)
        // RestAssured.basePath = "/v1";

        // Common request spec: JSON content type, accept header, relaxed HTTPS
        RestAssured.requestSpecification = new RequestSpecBuilder()
                .setBaseUri(RestAssured.baseURI)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .setRelaxedHTTPSValidation()
                .build();

        // Common response spec: expect any 2xx by default
        RestAssured.responseSpecification = new ResponseSpecBuilder()
                .expectStatusCode(greaterThanOrEqualTo(200))
                .expectStatusCode(lessThan(300))
                .build();

        // Enable logging when validation fails
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    /** Change the basePath just for this suite (call before tests). */
    protected void setBasePath(String path) {
        this.basePath = path == null ? "" : path;
        this.req = mutateSpec(rs -> rs.basePath(this.basePath));
    }

    /** Generic mutator: returns and stores a modified RequestSpecification. */
    protected RequestSpecification mutateSpec(UnaryOperator<RequestSpecification> mutator) {
        RequestSpecification newSpec = mutator.apply(this.req == null ? given() : this.req);
        this.req = newSpec;
        return newSpec;
    }

    /* ---------------------------------------------------
     * HTTP helpers (return Response so tests can assert)
     * --------------------------------------------------- */

    protected Response GET(String path) {
        return given().spec(req).when().get(path).then().extract().response();
    }

    protected Response GET(String path, Map<String, ?> queryParams) {
        return given().spec(req).queryParams(queryParams).when().get(path).then().extract().response();
    }

    protected Response POST(String path, Object body) {
        return given().spec(req).body(body).when().post(path).then().extract().response();
    }

    protected Response PUT(String path, Object body) {
        return given().spec(req).body(body).when().put(path).then().extract().response();
    }

    protected Response PATCH(String path, Object body) {
        return given().spec(req).body(body).when().patch(path).then().extract().response();
    }

    protected Response DELETE(String path) {
        return given().spec(req).when().delete(path).then().extract().response();
    }

    protected Response DELETE(String path, Object body) {
        return given().spec(req).body(body).when().delete(path).then().extract().response();
    }

    /* ---------------------------------------------------
     * Retry helper for flaky endpoints (optional)
     * --------------------------------------------------- */

    /**
     * Retry a request supplier up to 'attempts' until predicate says OK.
     */
    protected Response retry(int attempts, long sleepMillis, java.util.function.Supplier<Response> call,
                             java.util.function.Predicate<Response> ok) {
        RuntimeException lastEx = null;
        for (int i = 1; i <= attempts; i++) {
            try {
                Response r = call.get();
                if (ok.test(r)) return r;
            } catch (RuntimeException e) {
                lastEx = e;
            }
            try { Thread.sleep(sleepMillis); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
        }
        if (lastEx != null) throw lastEx;
        throw new AssertionError("Retry attempts exhausted without success");
    }

    /* ---------------------------------------------------
     * Internal: timeout selection
     * --------------------------------------------------- */

    private Duration getApiTimeout() {
        // Prefer -Dapi.timeout (e.g. 5s), else Config.timeout(), default 10s.
        String prop = System.getProperty("api.timeout", Config.get("api.timeout", ""));
        if (prop != null && !prop.isBlank()) {
            return parseDuration(prop.trim(), Duration.ofSeconds(10));
        }
        return Config.timeout(); // your Config already parses durations
    }

    private static Duration parseDuration(String s, Duration def) {
        String v = s.toLowerCase().trim();
        try {
            if (v.endsWith("ms")) return Duration.ofMillis(Long.parseLong(v.substring(0, v.length() - 2)));
            if (v.endsWith("s"))  return Duration.ofSeconds(Long.parseLong(v.substring(0, v.length() - 1)));
            if (v.endsWith("m"))  return Duration.ofMinutes(Long.parseLong(v.substring(0, v.length() - 1)));
            if (v.endsWith("h"))  return Duration.ofHours(Long.parseLong(v.substring(0, v.length() - 1)));
            return Duration.ofSeconds(Long.parseLong(v)); // plain number = seconds
        } catch (NumberFormatException e) {
            return def;
        }
    }
}

