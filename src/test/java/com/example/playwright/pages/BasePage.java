package com.example.playwright.pages;

import com.example.playwright.healing.AiHealingAdvisor;
import com.example.playwright.healing.ElementDefinition;
import com.example.playwright.healing.SelfHealingElement;
import com.microsoft.playwright.Page;

public abstract class BasePage {
    protected final Page page;
    private final AiHealingAdvisor aiHealingAdvisor;

    protected BasePage(Page page, AiHealingAdvisor aiHealingAdvisor) {
        this.page = page;
        this.aiHealingAdvisor = aiHealingAdvisor;
    }

    protected SelfHealingElement element(ElementDefinition definition) {
        return new SelfHealingElement(page, definition, aiHealingAdvisor);
    }
}
