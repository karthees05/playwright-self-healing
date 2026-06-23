package com.example.playwright.healing;

import com.microsoft.playwright.Page;

public record McpPageSnapshot(String url, String title, String visibleText, String interactiveElements) {
    public static McpPageSnapshot capture(Page page) {
        String visibleText = safe(() -> page.locator("body").innerText());
        String interactiveElements = safe(() -> (String) page.evaluate("""
                () => Array.from(document.querySelectorAll('input, button, a, textarea, select, [role]'))
                  .slice(0, 80)
                  .map((el, index) => {
                    const attrs = ['id', 'name', 'type', 'role', 'placeholder', 'aria-label', 'href']
                      .map(name => `${name}=${JSON.stringify(el.getAttribute(name))}`)
                      .join(' ');
                    const text = (el.innerText || el.value || '').trim().replace(/\\s+/g, ' ').slice(0, 80);
                    return `${index + 1}. <${el.tagName.toLowerCase()}> ${attrs} text=${JSON.stringify(text)}`;
                  })
                  .join('\\n')
                """));
        return new McpPageSnapshot(page.url(), safe(page::title), visibleText, interactiveElements);
    }

    public String toPrompt(String logicalElementName) {
        return """
                You are helping repair a Playwright Java locator.
                Target logical element: %s
                Current URL: %s
                Page title: %s

                Visible text:
                %s

                Interactive elements:
                %s

                Return the most stable locator recommendation using labels, roles, test ids, names, ids, or visible text.
                """.formatted(logicalElementName, url, title, clip(visibleText), interactiveElements);
    }

    private static String clip(String value) {
        if (value == null) {
            return "";
        }
        return value.length() <= 2000 ? value : value.substring(0, 2000);
    }

    private static String safe(SupplierWithException<String> supplier) {
        try {
            return supplier.get();
        } catch (RuntimeException ignored) {
            return "";
        }
    }

    @FunctionalInterface
    private interface SupplierWithException<T> {
        T get();
    }
}
