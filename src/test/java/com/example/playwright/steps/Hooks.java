package com.example.playwright.steps;

import com.example.playwright.core.DriverManager;
import com.example.playwright.core.Evidence;
import com.example.playwright.healing.HealingReport;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

import java.nio.charset.StandardCharsets;

public final class Hooks {
    /** Clears recovery events and starts an isolated browser session. */
    @Before
    public void beforeScenario() {
        HealingReport.clear();
        DriverManager.start();
    }

    /** Attaches recovery details and final status, captures failures, and always releases browser resources. */
    @After
    public void afterScenario(Scenario scenario) {
        try {
            if (HealingReport.hasEvents()) {
                HealingReport.recordAgentAttempt(scenario.getName(), java.util.List.of(),
                        "Final scenario status: " + scenario.getStatus());
                scenario.log(HealingReport.render());
                scenario.attach(HealingReport.render().getBytes(StandardCharsets.UTF_8),
                        "text/plain",
                        "llm-agent-repair-report.txt");
            }
            if (scenario.isFailed()) {
                Evidence.captureFailure(scenario.getName());
            }
        } finally {
            HealingReport.clear();
            DriverManager.stop();
        }
    }
}
