package com.example.playwright.healing;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;

import java.util.ArrayList;
import java.util.List;

public final class SelfHealingElement {
    private static final LlmHealingAgent LOCATOR_RECOVERY = new LlmHealingAgent();

    private final Page page;
    private final ElementDefinition definition;

    /** Associates the primary locator and intent hints with a browser page. */
    public SelfHealingElement(Page page, ElementDefinition definition) {
        this.page = page;
        this.definition = definition;
    }

    /** Clicks the element, investigating recovery only when the primary locator has no matches. */
    public void click() {
        withHealingVoid(Locator::click);
    }

    /** Fills the element with the supplied value, using recovery for a missing primary locator. */
    public void fill(String value) {
        withHealingVoid(locator -> locator.fill(value));
    }

    /** Reads element text through the primary or validated replacement locator. */
    public String textContent() {
        return withHealing(Locator::textContent);
    }

    /** Checks visibility; a missing primary locator may trigger recovery. */
    public boolean isVisible() {
        return Boolean.TRUE.equals(withHealing(Locator::isVisible));
    }

    /** Adapts a void action to the shared recovery workflow. */
    private void withHealingVoid(LocatorVoidAction action) {
        withHealing(locator -> {
            action.apply(locator);
            return null;
        });
    }

    /** Tries the primary locator, investigates zero matches, records a proposal, and retries once. */
    private <T> T withHealing(LocatorAction<T> action) {
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
            // An action may already have had side effects. Only missing locators trigger repair.
            throw e;
        }

        HealingDecision decision = LOCATOR_RECOVERY.recover(page, definition);
        LocatorStrategy recoveryStrategy = decision.strategy();
        try {
            Locator locator = recoveryStrategy.resolve(page);
            if (locator.count() == 1) {
                String beforeActionUrl = page.url();
                HealingReport.recordAgentRecovery(
                        definition.logicalName(),
                        recoveryStrategy.name(),
                        skippedStrategies,
                        decision.generatedCandidates(),
                        beforeActionUrl + " -> " + page.url(),
                        definition.hints(),
                        decision.recoveryDetails()
                );
                return action.apply(locator);
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

                LLM healing agent did not produce a usable locator candidate.
                """.formatted(definition.logicalName(), strategyNames()), lastFailure);
    }

    /** Shortens a Playwright error to one diagnostic line. */
    private String firstLine(String message) {
        if (message == null || message.isBlank()) {
            return "failed";
        }
        return message.lines().findFirst().orElse("failed");
    }

    /** Formats the primary locator and hints for unresolved-element errors. */
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
        /** Executes the caller's original operation on the resolved locator. */
        T apply(Locator locator);
    }

    @FunctionalInterface
    private interface LocatorVoidAction {
        /** Executes the caller's original operation on the resolved locator. */
        void apply(Locator locator);
    }
}
