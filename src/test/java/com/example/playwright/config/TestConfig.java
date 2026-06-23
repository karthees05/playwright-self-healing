package com.example.playwright.config;

import java.nio.file.Path;

public final class TestConfig {
    private TestConfig() {
    }

    public static String baseUrl() {
        return get("BASE_URL", "https://practicetestautomation.com");
    }

    public static boolean headless() {
        return Boolean.parseBoolean(get("HEADLESS", "true"));
    }

    public static int timeoutMillis() {
        return Integer.parseInt(get("PW_TIMEOUT_MS", "10000"));
    }

    public static boolean aiHealingEnabled() {
        return Boolean.parseBoolean(get("AI_HEALING_ENABLED", "false"));
    }

    public static String aiEndpoint() {
        return get("AI_ENDPOINT", "https://api.openai.com/v1/chat/completions");
    }

    public static String aiModel() {
        return get("OPENAI_MODEL", "gpt-4.1-mini");
    }

    public static String openAiApiKey() {
        return System.getenv("OPENAI_API_KEY");
    }

    public static Path evidenceDir() {
        return Path.of(get("EVIDENCE_DIR", "build/evidence"));
    }

    private static String get(String key, String fallback) {
        String value = System.getProperty(key);
        if (value == null || value.isBlank()) {
            value = System.getenv(key);
        }
        return value == null || value.isBlank() ? fallback : value;
    }
}
