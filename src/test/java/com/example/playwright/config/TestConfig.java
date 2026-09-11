package com.example.playwright.config;

import java.nio.file.Path;

public final class TestConfig {
    /** Prevents instantiation of this configuration utility. */
    private TestConfig() {
    }

    /** Returns the application URL, defaulting to the public login demo. */
    public static String baseUrl() {
        return get("BASE_URL", "https://practicetestautomation.com");
    }

    /** Determines whether browsers run without a visible window. */
    public static boolean headless() {
        return Boolean.parseBoolean(get("HEADLESS", "true"));
    }

    /** Returns the configured action and navigation timeout in milliseconds. */
    public static int timeoutMillis() {
        return Integer.parseInt(get("PW_TIMEOUT_MS", "10000"));
    }

    /** Returns the executable used to launch the MCP server. */
    public static String playwrightMcpCommand() {
        return get("PLAYWRIGHT_MCP_COMMAND", "npx");
    }

    /** Returns the configured MCP package and version. */
    public static String playwrightMcpPackage() {
        return get("PLAYWRIGHT_MCP_PACKAGE", "@playwright/mcp@latest");
    }

    /** Returns the directory for screenshots and DOM snapshots. */
    public static Path evidenceDir() {
        return Path.of(get("EVIDENCE_DIR", "build/evidence"));
    }

    /** Reads a system property, then an environment variable, then the supplied default. */
    private static String get(String key, String fallback) {
        String value = System.getProperty(key);
        if (value == null || value.isBlank()) {
            value = System.getenv(key);
        }
        return value == null || value.isBlank() ? fallback : value;
    }
}
