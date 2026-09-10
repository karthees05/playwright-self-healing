package com.example.playwright.healing;

import com.microsoft.playwright.Page;

import java.util.List;

public final class AiSelfHealingAgent {
    private static PlaywrightMcpClient mcpClient;

    public HealingDecision heal(Page page, ElementDefinition definition) {
        PlaywrightMcpClient client = client();
        McpLocatorRecommendation recommendation = client.generateLocator(page.url(), definition)
                .orElseThrow(() -> new IllegalStateException(
                        "AI self-healing agent could not get a locator from Playwright MCP for "
                                + definition.logicalName()));
        LocatorCandidate candidate = PlaywrightLocatorExpression.toCandidate(recommendation.locatorExpression())
                .orElseThrow(() -> new IllegalStateException(
                        "AI self-healing agent received an unsupported Playwright MCP locator expression: "
                                + recommendation.locatorExpression()));

        String selectedStrategy = "AI agent selected Playwright MCP "
                + recommendation.locatorExpression()
                + " target=" + recommendation.targetRef();

        return new HealingDecision(
                new LocatorStrategy(selectedStrategy, candidate),
                List.of(selectedStrategy),
                "The agent used Playwright MCP browser_find to identify the element reference, "
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
