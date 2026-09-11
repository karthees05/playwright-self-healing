package com.example.playwright.healing;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

@FunctionalInterface
public interface LocatorCandidate {
    /** Creates a locator on the supplied page without executing an action. */
    Locator create(Page page);
}
