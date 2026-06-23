package com.example.playwright.pages;

import com.example.playwright.config.TestConfig;
import com.example.playwright.healing.AiHealingAdvisor;
import com.example.playwright.healing.ElementDefinition;
import com.example.playwright.healing.LocatorStrategy;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.util.List;

public final class LoginPage extends BasePage {
    private static final ElementDefinition USERNAME = new ElementDefinition("login username field", List.of(
            new LocatorStrategy("css input#username", page -> page.locator("input#username")),
            new LocatorStrategy("input name=username", page -> page.locator("input[name='username']")),
            new LocatorStrategy("placeholder Username", page -> page.getByPlaceholder("Username"))
    ));

    private static final ElementDefinition PASSWORD = new ElementDefinition("login password field", List.of(
            new LocatorStrategy("css input#password", page -> page.locator("input#password")),
            new LocatorStrategy("input name=password", page -> page.locator("input[name='password']")),
            new LocatorStrategy("placeholder Password", page -> page.getByPlaceholder("Password"))
    ));

    private static final ElementDefinition SUBMIT = new ElementDefinition("login submit button", List.of(
            new LocatorStrategy("button#submit-broken-for-healing-demo", page -> page.locator("button#submit-broken-for-healing-demo")),
            new LocatorStrategy("role button Submit", page -> page.getByRole(AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName("Submit"))),
            new LocatorStrategy("button text Submit", page -> page.locator("button").filter(
                    new LocatorFilterOptions("Submit").toOptions()))
    ));

    private static final ElementDefinition ERROR = new ElementDefinition("login error message", List.of(
            new LocatorStrategy("css #error", page -> page.locator("#error")),
            new LocatorStrategy("text invalid", page -> page.getByText("invalid")),
            new LocatorStrategy("text error", page -> page.getByText("error"))
    ));

    public LoginPage(Page page, AiHealingAdvisor aiHealingAdvisor) {
        super(page, aiHealingAdvisor);
    }

    public void open() {
        page.navigate(TestConfig.baseUrl() + "/practice-test-login/");
    }

    public void login(String username, String password) {
        element(USERNAME).fill(username);
        element(PASSWORD).fill(password);
        element(SUBMIT).click();
    }

    public String errorMessage() {
        return element(ERROR).textContent();
    }
}
