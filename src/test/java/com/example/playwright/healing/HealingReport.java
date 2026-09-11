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
        recordMcpRecovery(logicalName, usedStrategy, skippedStrategies, List.of(), "", List.of());
    }

    public static void recordMcpRecovery(
            String logicalName,
            String usedStrategy,
            List<String> skippedStrategies,
            List<String> generatedCandidates,
            String pageUrl,
            List<String> hints
    ) {
        recordMcpRecovery(logicalName, usedStrategy, skippedStrategies, generatedCandidates, pageUrl, hints, "");
    }

    public static void recordMcpRecovery(
            String logicalName,
            String usedStrategy,
            List<String> skippedStrategies,
            List<String> generatedCandidates,
            String pageUrl,
            List<String> hints,
            String recoveryDetails
    ) {
        StringBuilder event = new StringBuilder()
                .append("Element: ").append(logicalName).append(System.lineSeparator())
                .append("Page URL: ").append(pageUrl).append(System.lineSeparator())
                .append("Recovered by: ").append(usedStrategy).append(System.lineSeparator())
                .append("Skipped strategies:").append(System.lineSeparator());

        for (String skippedStrategy : skippedStrategies) {
            event.append("- ").append(skippedStrategy).append(System.lineSeparator());
        }

        if (!hints.isEmpty()) {
            event.append("MCP intent hints:").append(System.lineSeparator());
            for (String hint : hints) {
                event.append("- ").append(hint).append(System.lineSeparator());
            }
        }

        if (recoveryDetails != null && !recoveryDetails.isBlank()) {
            event.append("Recovery details:").append(System.lineSeparator())
                    .append(recoveryDetails).append(System.lineSeparator());
        }

        if (!generatedCandidates.isEmpty()) {
            event.append("Generated MCP candidates:").append(System.lineSeparator());
            for (String generatedCandidate : generatedCandidates) {
                event.append("- ").append(generatedCandidate).append(System.lineSeparator());
            }
        }

        EVENTS.get().add(event.toString());
    }

    public static boolean hasEvents() {
        return !EVENTS.get().isEmpty();
    }

    public static String render() {
        StringBuilder report = new StringBuilder("MCP-based locator recovery events").append(System.lineSeparator());
        List<String> events = EVENTS.get();
        for (int i = 0; i < events.size(); i++) {
            report.append(System.lineSeparator())
                    .append("Event ").append(i + 1).append(System.lineSeparator())
                    .append(events.get(i));
        }
        return report.toString();
    }
}
