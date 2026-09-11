package com.example.playwright.healing;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public record LocatorStrategy(String name, LocatorCandidate candidate) {
    /** Creates the named strategy's Playwright locator on the supplied page. */
    public Locator resolve(Page page) {
        return candidate.create(page);
    }
}
