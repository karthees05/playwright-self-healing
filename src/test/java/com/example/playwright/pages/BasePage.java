package com.example.playwright.pages;

import com.example.playwright.healing.ElementDefinition;
import com.example.playwright.healing.SelfHealingElement;
import com.microsoft.playwright.Page;

public abstract class BasePage {
    protected final Page page;

    /** Binds this page object to the supplied browser page. */
    protected BasePage(Page page) {
        this.page = page;
    }

    /** Wraps an element definition with locator recovery behavior. */
    protected SelfHealingElement element(ElementDefinition definition) {
        return new SelfHealingElement(page, definition);
    }
}
