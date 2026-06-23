package com.example.playwright.steps;

import com.example.playwright.core.DriverManager;
import com.example.playwright.core.Evidence;
import com.example.playwright.healing.HealingReport;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

import java.nio.charset.StandardCharsets;

public final class Hooks {
    @Before
    public void beforeScenario() {
        HealingReport.clear();
        DriverManager.start();
    }

    @After
    public void afterScenario(Scenario scenario) {
        try {
            if (HealingReport.hasEvents()) {
                scenario.log(HealingReport.render());
                scenario.attach(HealingReport.render().getBytes(StandardCharsets.UTF_8),
                        "text/plain",
                        "self-healing-report.txt");
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
