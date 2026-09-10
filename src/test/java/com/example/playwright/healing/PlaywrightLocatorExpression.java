package com.example.playwright.healing;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PlaywrightLocatorExpression {
    private static final Pattern GET_BY_ROLE = Pattern.compile("getByRole\\('([^']+)', \\{ name: '([^']+)' }\\)");
    private static final Pattern GET_BY_TEXT = Pattern.compile("getByText\\('([^']+)'\\)");
    private static final Pattern GET_BY_LABEL = Pattern.compile("getByLabel\\('([^']+)'\\)");
    private static final Pattern GET_BY_PLACEHOLDER = Pattern.compile("getByPlaceholder\\('([^']+)'\\)");
    private static final Pattern LOCATOR = Pattern.compile("locator\\('([^']+)'\\)");

    private PlaywrightLocatorExpression() {
    }

    public static Optional<LocatorCandidate> toCandidate(String expression) {
        return match(expression, GET_BY_ROLE)
                .map(match -> (LocatorCandidate) page -> page.getByRole(toRole(match.group(1)),
                        new Page.GetByRoleOptions().setName(match.group(2))))
                .or(() -> match(expression, GET_BY_TEXT)
                        .map(match -> (LocatorCandidate) page -> page.getByText(match.group(1))))
                .or(() -> match(expression, GET_BY_LABEL)
                        .map(match -> (LocatorCandidate) page -> page.getByLabel(match.group(1))))
                .or(() -> match(expression, GET_BY_PLACEHOLDER)
                        .map(match -> (LocatorCandidate) page -> page.getByPlaceholder(match.group(1))))
                .or(() -> match(expression, LOCATOR)
                        .map(match -> (LocatorCandidate) page -> page.locator(match.group(1))));
    }

    private static Optional<Matcher> match(String expression, Pattern pattern) {
        Matcher matcher = pattern.matcher(expression);
        return matcher.find() ? Optional.of(matcher) : Optional.empty();
    }

    private static AriaRole toRole(String role) {
        return AriaRole.valueOf(role.toUpperCase(Locale.ROOT).replace('-', '_'));
    }
}
