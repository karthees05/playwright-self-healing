package com.example.playwright.pages;

import com.example.playwright.config.TestConfig;
import com.example.playwright.healing.ElementDefinition;
import com.example.playwright.healing.LocatorStrategy;
import com.microsoft.playwright.Page;

public final class LoginPage extends BasePage {
    private static final ElementDefinition USERNAME = new ElementDefinition(
            "login username field",
            new LocatorStrategy("css input#username", page -> page.locator("input#username")),
            "Username"
    );

    private static final ElementDefinition PASSWORD = new ElementDefinition(
            "login password field",
            new LocatorStrategy("css input#password", page -> page.locator("input#password")),
            "Password"
    );

    private static final ElementDefinition SUBMIT = new ElementDefinition(
            "login submit button",
            new LocatorStrategy("button#submit-broken-for-healing-demo", page -> page.locator("button#submit-broken-for-healing-demo")),
            "Submit"
    );

    private static final ElementDefinition ERROR = new ElementDefinition(
            "login error message",
            new LocatorStrategy("css #error", page -> page.locator("#error")),
            "invalid",
            "error"
    );

    /** Binds login interactions to the scenario's browser page. */
    public LoginPage(Page page) {
        super(page);
    }

    /** Navigates to the practice login page under the configured base URL. */
    public void open() {
        page.navigate(TestConfig.baseUrl() + "/practice-test-login/");
    }

    /** Fills credentials and clicks submit through the recovery wrapper. */
    public void login(String username, String password) {
        element(USERNAME).fill(username);
        element(PASSWORD).fill(password);
        element(SUBMIT).click();
    }

    /** Reads the login error text for scenario assertions. */
    public String errorMessage() {
        return element(ERROR).textContent();
    }
}
