package com.example.playwright.core;

import com.example.playwright.config.TestConfig;
import com.microsoft.playwright.BrowserType;

import java.util.List;

final class BrowserTypeOptions {
    /** Builds Chromium launch settings with configured headless mode and shared-memory options. */
    BrowserType.LaunchOptions toLaunchOptions() {
        return new BrowserType.LaunchOptions()
                .setHeadless(TestConfig.headless())
                .setArgs(List.of("--disable-dev-shm-usage"));
    }
}
