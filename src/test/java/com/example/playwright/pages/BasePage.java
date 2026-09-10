package com.example.playwright.pages;

import com.example.playwright.healing.ElementDefinition;
import com.example.playwright.healing.SelfHealingElement;
import com.microsoft.playwright.Page;

public abstract class BasePage {
    protected final Page page;

    protected BasePage(Page page) {
        this.page = page;
    }

    protected SelfHealingElement element(ElementDefinition definition) {
        return new SelfHealingElement(page, definition);
    }
}
