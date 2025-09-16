package com.example.utils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Centralized configuration loader.
 *
 * Load order (lowest -> highest precedence):
 *  1) classpath: config/common.properties
 *  2) classpath: config/{env}.properties   (env from -Denv=..., default 'dev')
 *  3) classpath: config/local.properties   (optional, ignored if missing)
 *  4) Environment variables (FOO_BAR -> foo.bar)
 *  5) JVM -D system properties
 *
 * Features:
 *  - ${var} placeholder resolution (recursive, with loop protection)
 *  - Aliases for legacy keys (e.g., base.url.api -> api.base.url)
 *  - Typed getters + convenience accessors
 */
public final class Config {

    private static final Properties PROPS = new Properties();

    // --- keys/env ---
    private static final String ENV_KEY = "env";
    private static final String DEFAULT_ENV = "dev";

    // --- alias map for legacy or alternate spellings ---
    private static final Map<String, String> ALIASES = Map.of(
            "base.url.api", "api.base.url",
            "base.url.ui",  "base.url",
            "apitimeout",   "api.timeout"
    );

    // --- placeholder pattern ${key} ---
    private static final Pattern VAR = Pattern.compile("\\$\\{([^}]+)}");
    private static final int MAX_SUBST_DEPTH = 10;

    static {
        load(null); // initial load with default env detection
    }

    private Config() {}

    /* ===================================
       Public API
     =================================== */

    /** Reload using the current env (or default). */
    public static synchronized void reload() {
        load(null);
    }

    /** Explicitly (re)load with an env override, e.g., "qa" or "prod". */
    public static synchronized void load(String envOverride) {
        PROPS.clear();

        // 1) common
        loadProps("config/common.properties");

        // figure out env
        String env = (envOverride != null && !envOverride.isBlank())
                ? envOverride
                : System.getProperty(ENV_KEY, PROPS.getProperty(ENV_KEY, DEFAULT_ENV));
        env = env.toLowerCase(Locale.ROOT);
        PROPS.setProperty(ENV_KEY, env); // ensure set

        // 2) env-specific
        loadProps("config/" + env + ".properties");

        // 3) local overrides
        loadProps("config/local.properties");

        // 4) env vars (FOO_BAR -> foo.bar)
        for (Map.Entry<String, String> e : System.getenv().entrySet()) {
            String k = e.getKey().toLowerCase(Locale.ROOT).replace('_', '.');
            PROPS.put(k, e.getValue());
        }

        // 5) system props
        for (Map.Entry<Object, Object> e : System.getProperties().entrySet()) {
            PROPS.put(String.valueOf(e.getKey()), String.valueOf(e.getValue()));
        }

        // resolve ${...}
        resolveAllPlaceholders();
    }

    /** Get required string; throws if missing. */
    public static String get(String key) {
        String k = ALIASES.getOrDefault(key, key);
        String v = PROPS.getProperty(k);
        if (v == null) throw new NoSuchElementException("Missing config key: " + key);
        return v.trim();
    }

    /** Get string with default. */
    public static String get(String key, String def) {
        String k = ALIASES.getOrDefault(key, key);
        String v = PROPS.getProperty(k);
        return (v == null) ? def : v.trim();
    }

    public static int getInt(String key, int def) {
        String v = getOrNull(key);
        return (v == null) ? def : Integer.parseInt(v);
    }

    public static long getLong(String key, long def) {
        String v = getOrNull(key);
        return (v == null) ? def : Long.parseLong(v);
    }

    public static boolean getBool(String key, boolean def) {
        String v = getOrNull(key);
        if (v == null) return def;
        v = v.trim().toLowerCase(Locale.ROOT);
        return v.equals("true") || v.equals("yes") || v.equals("1");
    }

    /** Parse "500ms", "10s", "2m", "1h", or plain number = seconds. */
    public static Duration getDuration(String key, Duration def) {
        String v = getOrNull(key);
        if (v == null || v.isBlank()) return def;
        return parseDuration(v);
    }

    public static List<String> getList(String key, String separatorRegex, List<String> def) {
        String v = getOrNull(key);
        if (v == null) return def;
        String[] parts = v.split(separatorRegex);
        List<String> out = new ArrayList<>(parts.length);
        for (String p : parts) {
            String t = p.trim();
            if (!t.isEmpty()) out.add(t);
        }
        return out;
    }

    // --- convenience accessors ---

    public static String env() {
        return get(ENV_KEY, DEFAULT_ENV).toLowerCase(Locale.ROOT);
    }

    public static String baseUrl() {
        return get("base.url", "https://www.saucedemo.com");
    }

    public static String apiBaseUrl() {
        return get("api.base.url", "https://fakestoreapi.com");
    }

    /** Global timeout (UI waits etc.). Falls back to 10s. */
    public static Duration timeout() {
        return parseDuration(get("timeout", "10s"));
    }

    /** API timeout. Falls back to timeout(), then 10s. */
    public static Duration apiTimeout() {
        String raw = get("api.timeout", "");
        if (!raw.isBlank()) return parseDuration(raw);
        return timeout();
    }

    /** Defensive copy of all resolved props (useful for debugging). */
    public static Properties raw() {
        Properties copy = new Properties();
        copy.putAll(PROPS);
        return copy;
    }

    /* ===================================
       Internals
     =================================== */

    private static String getOrNull(String key) {
        String k = ALIASES.getOrDefault(key, key);
        String v = PROPS.getProperty(k);
        return (v == null) ? null : v.trim();
    }

    private static void loadProps(String cpPath) {
        try (InputStream in = resource(cpPath)) {
            if (in != null) {
                try (InputStreamReader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                    PROPS.load(r);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load properties: " + cpPath, e);
        }
    }

    private static InputStream resource(String path) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) cl = Config.class.getClassLoader();
        return cl.getResourceAsStream(path);
    }

    private static void resolveAllPlaceholders() {
        List<String> keys = new ArrayList<>();
        for (Object k : PROPS.keySet()) keys.add(String.valueOf(k));
        for (String k : keys) {
            String v = PROPS.getProperty(k);
            if (v != null) PROPS.setProperty(k, resolvePlaceholders(v, 0));
        }
    }

    private static String resolvePlaceholders(String value, int depth) {
        if (depth > MAX_SUBST_DEPTH) {
            throw new IllegalStateException("Too many nested placeholders: " + value);
        }
        Matcher m = VAR.matcher(value);
        StringBuffer sb = new StringBuffer();
        boolean found = false;
        while (m.find()) {
            found = true;
            String key = m.group(1);
            String aliasKey = ALIASES.getOrDefault(key, key);
            String repl = PROPS.getProperty(aliasKey);
            if (repl == null) {
                // also check sys props and env vars (FOO_BAR style)
                repl = System.getProperty(aliasKey);
                if (repl == null) repl = System.getenv(keyToEnv(aliasKey));
            }
            if (repl == null) repl = ""; // unresolved => empty
            m.appendReplacement(sb, Matcher.quoteReplacement(repl));
        }
        m.appendTail(sb);
        String out = sb.toString();
        return found ? resolvePlaceholders(out, depth + 1) : out;
    }

    private static String keyToEnv(String key) {
        return key.toUpperCase(Locale.ROOT).replace('.', '_');
    }

    /** Parse "500ms", "10s", "2m", "1h", or plain number (seconds). */
    private static Duration parseDuration(String value) {
        String v = value.toLowerCase(Locale.ROOT).trim();
        try {
            if (v.endsWith("ms")) return Duration.ofMillis(Long.parseLong(v.substring(0, v.length() - 2)));
            if (v.endsWith("s"))  return Duration.ofSeconds(Long.parseLong(v.substring(0, v.length() - 1)));
            if (v.endsWith("m"))  return Duration.ofMinutes(Long.parseLong(v.substring(0, v.length() - 1)));
            if (v.endsWith("h"))  return Duration.ofHours(Long.parseLong(v.substring(0, v.length() - 1)));
            return Duration.ofSeconds(Long.parseLong(v)); // plain number = seconds
        } catch (NumberFormatException e) {
            return Duration.ofSeconds(10);
        }
    }
}
