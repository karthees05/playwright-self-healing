package com.example.playwright.pages;

import com.example.playwright.healing.AiHealingAdvisor;
import com.example.playwright.healing.ElementDefinition;
import com.example.playwright.healing.LocatorStrategy;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.util.List;

public final class SecurePage extends BasePage {
    private static final ElementDefinition SUCCESS_MESSAGE = new ElementDefinition("secure area success message", List.of(
            new LocatorStrategy("css .post-title", page -> page.locator(".post-title")),
            new LocatorStrategy("heading Logged In Successfully", page -> page.getByRole(AriaRole.HEADING,
                    new Page.GetByRoleOptions().setName("Logged In Successfully"))),
            new LocatorStrategy("text Logged In Successfully", page -> page.getByText("Logged In Successfully"))
    ));

    private static final ElementDefinition LOGOUT = new ElementDefinition("secure area logout button", List.of(
            new LocatorStrategy("text Log out", page -> page.getByText("Log out")),
            new LocatorStrategy("role link Log out", page -> page.getByRole(AriaRole.LINK,
                    new Page.GetByRoleOptions().setName("Log out"))),
            new LocatorStrategy("href practice-test-login", page -> page.locator("a[href*='practice-test-login']"))
    ));

    public SecurePage(Page page, AiHealingAdvisor aiHealingAdvisor) {
        super(page, aiHealingAdvisor);
    }

    public boolean isDisplayed() {
        return element(SUCCESS_MESSAGE).isVisible();
    }

    public void logout() {
        element(LOGOUT).click();
    }
}
