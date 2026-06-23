package com.example.playwright.healing;

import java.util.List;

public record ElementDefinition(String logicalName, List<LocatorStrategy> strategies) {
    public ElementDefinition {
        if (logicalName == null || logicalName.isBlank()) {
            throw new IllegalArgumentException("logicalName is required");
        }
        if (strategies == null || strategies.isEmpty()) {
            throw new IllegalArgumentException("At least one locator strategy is required for " + logicalName);
        }
    }
}
