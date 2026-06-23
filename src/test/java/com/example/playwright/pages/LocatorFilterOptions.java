package com.example.playwright.pages;

import com.microsoft.playwright.Locator;

final class LocatorFilterOptions {
    private final String text;

    LocatorFilterOptions(String text) {
        this.text = text;
    }

    Locator.FilterOptions toOptions() {
        return new Locator.FilterOptions().setHasText(text);
    }
}
