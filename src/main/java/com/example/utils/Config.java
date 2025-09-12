package com.example.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
    private static final Properties props = new Properties();

    public static void load(String env) {
        try (FileInputStream in = new FileInputStream("src/main/resources/config-" + env.toLowerCase() + ".properties")) {
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Cannot load config for env: " + env, e);
        }
    }

    public static String get(String key) {
        String v = props.getProperty(key);
        if (v == null) throw new RuntimeException("Missing config key: " + key);
        return v;
    }

    public static String getOrDefault(String key, String def) {
        return props.getProperty(key, def);
    }

    public static int getInt(String key, int def) {
        try { return Integer.parseInt(props.getProperty(key, String.valueOf(def))); }
        catch (Exception e){ return def; }
    }
}
