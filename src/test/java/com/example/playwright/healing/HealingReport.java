package com.example.playwright.healing;

import java.util.ArrayList;
import java.util.List;

public final class HealingReport {
    private static final ThreadLocal<List<String>> EVENTS = ThreadLocal.withInitial(ArrayList::new);

    /** Prevents instantiation of this thread-local report utility. */
    private HealingReport() {
    }

    /** Removes report events for the current scenario thread. */
    public static void clear() {
        EVENTS.remove();
    }

    /** Appends an investigation trace and its outcome or final scenario status. */
    public static void recordAgentAttempt(String element, List<String> trace, String outcome) {
        EVENTS.get().add("Element: " + element + "\n" + String.join("\n", trace) + "\n" + outcome);
    }

    /** Records a basic recovery event with the selected and skipped strategies. */
    public static void recordFallback(String logicalName, String usedStrategy, List<String> skippedStrategies) {
        recordAgentRecovery(logicalName, usedStrategy, skippedStrategies, List.of(), "", List.of());
    }

    /** Records a selected locator and available recovery context before retry success is known. */
    public static void recordAgentRecovery(
            String logicalName,
            String usedStrategy,
            List<String> skippedStrategies,
            List<String> generatedCandidates,
            String pageUrl,
            List<String> hints
    ) {
        recordAgentRecovery(logicalName, usedStrategy, skippedStrategies, generatedCandidates, pageUrl, hints, "");
    }

    /** Records a selected locator and available recovery context before retry success is known. */
    public static void recordAgentRecovery(
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
                .append("Repair selected by (retry pending): ").append(usedStrategy).append(System.lineSeparator())
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
            event.append("Agent investigation summary:").append(System.lineSeparator())
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

    /** Checks whether the scenario has recovery information to attach. */
    public static boolean hasEvents() {
        return !EVENTS.get().isEmpty();
    }

    /** Formats the current thread's events into a numbered plain-text report. */
    public static String render() {
        StringBuilder report = new StringBuilder("LLM agent locator repair events").append(System.lineSeparator());
        List<String> events = EVENTS.get();
        for (int i = 0; i < events.size(); i++) {
            report.append(System.lineSeparator())
                    .append("Event ").append(i + 1).append(System.lineSeparator())
                    .append(events.get(i));
        }
        return report.toString();
    }
}
