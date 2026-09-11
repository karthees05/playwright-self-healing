package com.example.playwright.healing;

import java.util.List;

public record ElementDefinition(String logicalName, LocatorStrategy primaryStrategy, List<String> hints) {
    /** Builds an element definition with immutable hints and validates required fields. */
    public ElementDefinition(String logicalName, LocatorStrategy primaryStrategy, String... hints) {
        this(logicalName, primaryStrategy, List.of(hints));
    }

    /** Builds an element definition with immutable hints and validates required fields. */
    public ElementDefinition {
        if (logicalName == null || logicalName.isBlank()) {
            throw new IllegalArgumentException("logicalName is required");
        }
        if (primaryStrategy == null) {
            throw new IllegalArgumentException("primaryStrategy is required for " + logicalName);
        }
        hints = hints == null ? List.of() : List.copyOf(hints);
    }
}
