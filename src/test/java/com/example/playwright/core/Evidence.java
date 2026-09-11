package com.example.playwright.core;

import com.example.playwright.config.TestConfig;
import com.microsoft.playwright.Page;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class Evidence {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");

    /** Prevents instantiation of this evidence utility. */
    private Evidence() {
    }

    /** Saves timestamped screenshots and DOM evidence without replacing the test failure. */
    public static void captureFailure(String scenarioName) {
        Page page = DriverManager.page();
        Path scenarioDir = TestConfig.evidenceDir().resolve(safeName(scenarioName));
        try {
            Files.createDirectories(scenarioDir);
            String stamp = LocalDateTime.now().format(FORMATTER);
            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(scenarioDir.resolve("failure-" + stamp + ".png"))
                    .setFullPage(true));
            Files.writeString(scenarioDir.resolve("dom-" + stamp + ".html"), page.content());
        } catch (IOException | RuntimeException ignored) {
            // Evidence is helpful but should never replace the real test failure.
        }
    }

    /** Converts a scenario name into a filesystem-friendly directory name. */
    private static String safeName(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }
}
