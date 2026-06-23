package com.example.playwright.healing;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

@FunctionalInterface
public interface LocatorCandidate {
    Locator create(Page page);
}
