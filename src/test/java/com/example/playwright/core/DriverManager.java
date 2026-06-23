package com.example.playwright.core;

import com.example.playwright.config.TestConfig;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

public final class DriverManager {
    private static final ThreadLocal<Playwright> PLAYWRIGHT = new ThreadLocal<>();
    private static final ThreadLocal<Browser> BROWSER = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> CONTEXT = new ThreadLocal<>();
    private static final ThreadLocal<Page> PAGE = new ThreadLocal<>();

    private DriverManager() {
    }

    public static void start() {
        Playwright playwright = Playwright.create();
        Browser browser = playwright.chromium().launch(new BrowserTypeOptions().toLaunchOptions());
        BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(1440, 900));
        Page page = context.newPage();
        page.setDefaultTimeout(TestConfig.timeoutMillis());

        PLAYWRIGHT.set(playwright);
        BROWSER.set(browser);
        CONTEXT.set(context);
        PAGE.set(page);
    }

    public static Page page() {
        Page page = PAGE.get();
        if (page == null) {
            throw new IllegalStateException("Playwright page is not initialized. Did the Cucumber hook run?");
        }
        return page;
    }

    public static void stop() {
        close(CONTEXT.get());
        close(BROWSER.get());
        close(PLAYWRIGHT.get());
        CONTEXT.remove();
        BROWSER.remove();
        PLAYWRIGHT.remove();
        PAGE.remove();
    }

    private static void close(AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception ignored) {
            // Cleanup should not hide the scenario failure.
        }
    }
}
