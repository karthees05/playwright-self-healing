package com.example.playwright.healing;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;

import java.util.ArrayList;
import java.util.List;

public final class SelfHealingElement {
    private final Page page;
    private final ElementDefinition definition;
    private final AiHealingAdvisor aiHealingAdvisor;

    public SelfHealingElement(Page page, ElementDefinition definition, AiHealingAdvisor aiHealingAdvisor) {
        this.page = page;
        this.definition = definition;
        this.aiHealingAdvisor = aiHealingAdvisor;
    }

    public void click() {
        withHealingVoid(Locator::click);
    }

    public void fill(String value) {
        withHealingVoid(locator -> locator.fill(value));
    }

    public String textContent() {
        return withHealing(Locator::textContent);
    }

    public boolean isVisible() {
        return Boolean.TRUE.equals(withHealing(Locator::isVisible));
    }

    private void withHealingVoid(LocatorVoidAction action) {
        withHealing(locator -> {
            action.apply(locator);
            return null;
        });
    }

    private <T> T withHealing(LocatorAction<T> action) {
        PlaywrightException lastFailure = null;
        List<String> skippedStrategies = new ArrayList<>();
        for (LocatorStrategy strategy : definition.strategies()) {
            try {
                Locator locator = strategy.resolve(page);
                if (locator.count() > 0) {
                    T result = action.apply(locator.first());
                    if (!skippedStrategies.isEmpty()) {
                        HealingReport.recordFallback(definition.logicalName(), strategy.name(), skippedStrategies);
                    }
                    return result;
                }
                skippedStrategies.add(strategy.name() + " (no matches)");
            } catch (PlaywrightException e) {
                lastFailure = e;
                skippedStrategies.add(strategy.name() + " (" + firstLine(e.getMessage()) + ")");
            }
        }

        PageSnapshot snapshot = PageSnapshot.capture(page);
        String advice = aiHealingAdvisor.advise(definition.logicalName(), snapshot);
        throw new AssertionError("""
                Unable to locate self-healing element: %s

                Tried strategies:
                %s

                AI healing advice:
                %s
                """.formatted(definition.logicalName(), strategyNames(), advice), lastFailure);
    }

    private String firstLine(String message) {
        if (message == null || message.isBlank()) {
            return "failed";
        }
        return message.lines().findFirst().orElse("failed");
    }

    private String strategyNames() {
        StringBuilder builder = new StringBuilder();
        for (LocatorStrategy strategy : definition.strategies()) {
            builder.append("- ").append(strategy.name()).append(System.lineSeparator());
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
