package com.example.playwright.healing;

import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class LlmHealingAgentTest {
    private static final String LOCATOR = "getByRole('button', { name: 'Submit' })";

    /** Verifies that rejection feedback reaches the model and a later valid proposal completes repair. */
    @Test void investigatesRejectedCandidateAndRepairs() throws Exception {
        var steps = new ArrayDeque<>(List.of(
                new LlmHealingAgent.Step("find", "Submit", "Inspect button"),
                new LlmHealingAgent.Step("generate", "e42", "Generate observed target"),
                new LlmHealingAgent.Step("propose", LOCATOR, "Check candidate"),
                new LlmHealingAgent.Step("snapshot", "", "Reinspect after rejection"),
                new LlmHealingAgent.Step("propose", LOCATOR, "Check refreshed state")));
        AtomicInteger validations = new AtomicInteger();
        var trace = new ArrayList<String>();
        var decision = LlmHealingAgent.investigate(evidence -> {
            if (steps.size() == 2) assertTrue(evidence.contains("ambiguous"));
            return steps.remove();
        }, (name, args) -> name.equals("browser_generate_locator") ? "### Result\n" + LOCATOR : "button Submit [ref=e42]",
                expression -> validations.incrementAndGet() == 1 ? "ambiguous" : "valid", "missing submit", trace);
        assertTrue(decision.strategy().name().contains(LOCATOR));
        assertEquals(2, validations.get());
        assertTrue(decision.recoveryDetails().contains("ambiguous"));
    }

    /** Verifies guessed locators bypass validation and repeated proposals exhaust the step budget. */
    @Test void rejectsUngeneratedLocatorAndStopsAtBudget() {
        AtomicInteger validations = new AtomicInteger();
        var trace = new ArrayList<String>();
        assertThrows(IllegalStateException.class, () -> LlmHealingAgent.investigate(
                evidence -> new LlmHealingAgent.Step("propose", LOCATOR, "guess"),
                (name, args) -> { fail("No tool should run"); return ""; },
                expression -> { validations.incrementAndGet(); return "valid"; }, "failure", trace));
        assertEquals(0, validations.get());
        assertEquals(16, trace.size());
    }

    /** Verifies forbidden actions and model refusal stop without browser side effects. */
    @Test void rejectsDisallowedActionAndHonorsAbort() {
        for (String action : List.of("browser_click", "abort")) {
            assertThrows(IllegalStateException.class, () -> LlmHealingAgent.investigate(
                    evidence -> new LlmHealingAgent.Step(action, "", "stop"),
                    (name, args) -> { fail("No browser side effects"); return ""; },
                    expression -> "valid", "failure", new ArrayList<>()));
        }
    }

    /** Verifies that locator syntax containing additional executable operations is rejected. */
    @Test void parserRejectsTrailingExecutableCode() {
        assertTrue(PlaywrightLocatorExpression.toCandidate(LOCATOR + ".click()").isEmpty());
        assertTrue(PlaywrightLocatorExpression.toCandidate("malicious(); " + LOCATOR).isEmpty());
    }
}
