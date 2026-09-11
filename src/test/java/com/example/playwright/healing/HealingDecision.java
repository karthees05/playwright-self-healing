package com.example.playwright.healing;

import java.util.List;

public record HealingDecision(
        LocatorStrategy strategy,
        List<String> generatedCandidates,
        String recoveryDetails
) {
}
