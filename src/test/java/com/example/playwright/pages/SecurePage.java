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

    /** Binds secure-area interactions to the supplied page. */
    public SecurePage(Page page) {
        super(page);
    }

    /** Checks visibility of the successful-login heading. */
    public boolean isDisplayed() {
        return element(SUCCESS_MESSAGE).isVisible();
    }

    /** Clicks the logout element through the recovery wrapper. */
    public void logout() {
        element(LOGOUT).click();
    }
}
