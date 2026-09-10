package com.example.playwright.healing;

public record McpLocatorRecommendation(
        String targetRef,
        String locatorExpression,
        String rawResponse,
        String source
) {
}
