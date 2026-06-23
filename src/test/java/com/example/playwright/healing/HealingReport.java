package com.example.playwright.healing;

import java.util.ArrayList;
import java.util.List;

public final class HealingReport {
    private static final ThreadLocal<List<String>> EVENTS = ThreadLocal.withInitial(ArrayList::new);

    private HealingReport() {
    }

    public static void clear() {
        EVENTS.remove();
    }

    public static void recordFallback(String logicalName, String usedStrategy, List<String> skippedStrategies) {
        StringBuilder event = new StringBuilder()
                .append("Element: ").append(logicalName).append(System.lineSeparator())
                .append("Healed by: ").append(usedStrategy).append(System.lineSeparator())
                .append("Skipped strategies:").append(System.lineSeparator());

        for (String skippedStrategy : skippedStrategies) {
            event.append("- ").append(skippedStrategy).append(System.lineSeparator());
        }

        EVENTS.get().add(event.toString());
    }

    public static boolean hasEvents() {
        return !EVENTS.get().isEmpty();
    }

    public static String render() {
        StringBuilder report = new StringBuilder("Self-healing locator events").append(System.lineSeparator());
        List<String> events = EVENTS.get();
        for (int i = 0; i < events.size(); i++) {
            report.append(System.lineSeparator())
                    .append("Event ").append(i + 1).append(System.lineSeparator())
                    .append(events.get(i));
        }
        return report.toString();
    }
}
