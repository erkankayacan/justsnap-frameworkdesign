package com.example.api;

import com.example.utils.Config;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.ConnectionConfig;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.LogConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.time.Duration;

public final class RequestSpecs {
    private RequestSpecs(){}

    public static RequestSpecification json(String basePath) {
        var t = apiTimeout();
        var cfg = RestAssuredConfig.newConfig()
                .logConfig(new LogConfig().enableLoggingOfRequestAndResponseIfValidationFails())
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", (int) t.toMillis())
                        .setParam("http.socket.timeout",     (int) t.toMillis())
                        .setParam("http.connection-manager.timeout", (int) t.toMillis()))
                .connectionConfig(new ConnectionConfig().closeIdleConnectionsAfterEachResponse());

        var b = new RequestSpecBuilder()
                .setBaseUri(Config.apiBaseUrl())
                .setBasePath(basePath)                 // <- per-request spec, not global
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .setRelaxedHTTPSValidation()
                .setConfig(cfg);

        if (Boolean.parseBoolean(System.getProperty("api.log", Config.get("api.log","true")))) {
            b.log(LogDetail.ALL);
        }
        return b.build();
    }

    private static Duration apiTimeout() {
        String v = System.getProperty("api.timeout", Config.get("api.timeout",""));
        if (v != null && !v.isBlank()) return parse(v.trim());
        return Config.timeout();
    }
    private static Duration parse(String s) {
        s = s.toLowerCase();
        if (s.endsWith("ms")) return Duration.ofMillis(Long.parseLong(s.substring(0,s.length()-2)));
        if (s.endsWith("s"))  return Duration.ofSeconds(Long.parseLong(s.substring(0,s.length()-1)));
        if (s.endsWith("m"))  return Duration.ofMinutes(Long.parseLong(s.substring(0,s.length()-1)));
        if (s.endsWith("h"))  return Duration.ofHours(Long.parseLong(s.substring(0,s.length()-1)));
        return Duration.ofSeconds(Long.parseLong(s));
    }
}
