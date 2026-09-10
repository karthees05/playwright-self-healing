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

    public static String playwrightMcpCommand() {
        return get("PLAYWRIGHT_MCP_COMMAND", "npx");
    }

    public static String playwrightMcpPackage() {
        return get("PLAYWRIGHT_MCP_PACKAGE", "@playwright/mcp@latest");
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
