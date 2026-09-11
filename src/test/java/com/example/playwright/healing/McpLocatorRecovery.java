package com.example.playwright.healing;

import com.microsoft.playwright.Page;

import java.util.List;

public final class McpLocatorRecovery {
    private static PlaywrightMcpClient mcpClient;

    public HealingDecision recover(Page page, ElementDefinition definition) {
        PlaywrightMcpClient client = client();
        McpLocatorRecommendation recommendation = client.generateLocator(page.url(), definition)
                .orElseThrow(() -> new IllegalStateException(
                        "MCP-based locator recovery could not get a locator from Playwright MCP for "
                                + definition.logicalName()));
        LocatorCandidate candidate = PlaywrightLocatorExpression.toCandidate(recommendation.locatorExpression())
                .orElseThrow(() -> new IllegalStateException(
                        "MCP-based locator recovery received an unsupported Playwright MCP locator expression: "
                                + recommendation.locatorExpression()));

        String selectedStrategy = "MCP-based locator recovery using Playwright MCP "
                + recommendation.locatorExpression()
                + " target=" + recommendation.targetRef();

        return new HealingDecision(
                new LocatorStrategy(selectedStrategy, candidate),
                List.of(selectedStrategy),
                "The recovery component used Playwright MCP browser_find to identify the element reference, "
                        + "then browser_generate_locator to create a stable Playwright locator."
        );
    }

    private static synchronized PlaywrightMcpClient client() {
        if (mcpClient == null) {
            mcpClient = PlaywrightMcpClient.start();
        }
        return mcpClient;
    }
}
