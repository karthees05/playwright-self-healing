package com.example.playwright.healing;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;

import java.util.ArrayList;
import java.util.List;

public final class SelfHealingElement {
    private static final McpLocatorRecovery LOCATOR_RECOVERY = new McpLocatorRecovery();

    private final Page page;
    private final ElementDefinition definition;

    public SelfHealingElement(Page page, ElementDefinition definition) {
        this.page = page;
        this.definition = definition;
    }

    public void click() {
        withHealingVoid(Locator::click, true);
    }

    public void fill(String value) {
        withHealingVoid(locator -> locator.fill(value), false);
    }

    public String textContent() {
        return withHealing(Locator::textContent, false);
    }

    public boolean isVisible() {
        return Boolean.TRUE.equals(withHealing(Locator::isVisible, false));
    }

    private void withHealingVoid(LocatorVoidAction action, boolean navigationCanMeanSuccess) {
        withHealing(locator -> {
            action.apply(locator);
            return null;
        }, navigationCanMeanSuccess);
    }

    private <T> T withHealing(LocatorAction<T> action, boolean navigationCanMeanSuccess) {
        PlaywrightException lastFailure = null;
        List<String> skippedStrategies = new ArrayList<>();
        LocatorStrategy primaryStrategy = definition.primaryStrategy();
        try {
            Locator locator = primaryStrategy.resolve(page);
            if (locator.count() > 0) {
                return action.apply(locator.first());
            }
            skippedStrategies.add(primaryStrategy.name() + " (no matches)");
        } catch (PlaywrightException e) {
            lastFailure = e;
            skippedStrategies.add(primaryStrategy.name() + " (" + firstLine(e.getMessage()) + ")");
        }

        HealingDecision decision = LOCATOR_RECOVERY.recover(page, definition);
        LocatorStrategy recoveryStrategy = decision.strategy();
        try {
            Locator locator = recoveryStrategy.resolve(page);
            if (locator.count() > 0) {
                String beforeActionUrl = page.url();
                T result = applyAction(action, locator.first(), navigationCanMeanSuccess);
                HealingReport.recordMcpRecovery(
                        definition.logicalName(),
                        recoveryStrategy.name(),
                        skippedStrategies,
                        decision.generatedCandidates(),
                        beforeActionUrl + " -> " + page.url(),
                        definition.hints(),
                        decision.recoveryDetails()
                );
                return result;
            }
            skippedStrategies.add(recoveryStrategy.name() + " (no matches)");
        } catch (PlaywrightException e) {
            lastFailure = e;
            skippedStrategies.add(recoveryStrategy.name() + " (" + firstLine(e.getMessage()) + ")");
        }

        throw new AssertionError("""
                Unable to locate self-healing element: %s

                Tried strategies:
                %s

                MCP-based locator recovery did not produce a usable locator candidate.
                """.formatted(definition.logicalName(), strategyNames()), lastFailure);
    }

    private <T> T applyAction(LocatorAction<T> action, Locator locator, boolean navigationCanMeanSuccess) {
        try {
            return action.apply(locator);
        } catch (PlaywrightException e) {
            if (navigationCanMeanSuccess && isNavigationDuringAction(e)) {
                return null;
            }
            throw e;
        }
    }

    private boolean isNavigationDuringAction(PlaywrightException e) {
        String message = e.getMessage();
        return message != null && message.contains("Execution context was destroyed");
    }

    private String firstLine(String message) {
        if (message == null || message.isBlank()) {
            return "failed";
        }
        return message.lines().findFirst().orElse("failed");
    }

    private String strategyNames() {
        StringBuilder builder = new StringBuilder();
        builder.append("- ").append(definition.primaryStrategy().name()).append(System.lineSeparator());
        for (String hint : definition.hints()) {
            builder.append("- MCP intent hint: ").append(hint).append(System.lineSeparator());
        }
        return builder.toString();
    }

    @FunctionalInterface
    private interface LocatorAction<T> {
        T apply(Locator locator);
    }

    @FunctionalInterface
    private interface LocatorVoidAction {
        void apply(Locator locator);
    }
}
