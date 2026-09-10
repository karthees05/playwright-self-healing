package com.example.playwright.pages;

import com.example.playwright.healing.ElementDefinition;
import com.example.playwright.healing.LocatorStrategy;
import com.microsoft.playwright.Page;

public final class SecurePage extends BasePage {
    private static final ElementDefinition SUCCESS_MESSAGE = new ElementDefinition(
            "secure area success message",
            new LocatorStrategy("css .post-title", page -> page.locator(".post-title")),
            "Logged In Successfully"
    );

    private static final ElementDefinition LOGOUT = new ElementDefinition(
            "secure area logout button",
            new LocatorStrategy("text Log out", page -> page.getByText("Log out")),
            "Log out",
            "Logout"
    );

    public SecurePage(Page page) {
        super(page);
    }

    public boolean isDisplayed() {
        return element(SUCCESS_MESSAGE).isVisible();
    }

    public void logout() {
        element(LOGOUT).click();
    }
}
