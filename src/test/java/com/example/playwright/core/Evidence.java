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

    private Evidence() {
    }

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

    private static String safeName(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }
}
