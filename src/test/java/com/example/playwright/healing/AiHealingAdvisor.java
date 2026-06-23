package com.example.playwright.healing;

import com.example.playwright.config.TestConfig;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class AiHealingAdvisor {
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public String advise(String logicalElementName, McpPageSnapshot snapshot) {
        String prompt = snapshot.toPrompt(logicalElementName);
        if (!TestConfig.aiHealingEnabled()) {
            return "AI healing disabled. Set AI_HEALING_ENABLED=true and OPENAI_API_KEY to request advice.\n\n" + prompt;
        }
        String apiKey = TestConfig.openAiApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            return "AI healing enabled, but OPENAI_API_KEY is missing.\n\n" + prompt;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(TestConfig.aiEndpoint()))
                    .timeout(Duration.ofSeconds(30))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody(prompt)))
                    .build();
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
        } catch (IOException e) {
            return "AI healing request failed: " + e.getMessage() + "\n\n" + prompt;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "AI healing request interrupted.\n\n" + prompt;
        }
    }

    private String requestBody(String prompt) {
        return """
                {
                  "model": "%s",
                  "messages": [
                    {
                      "role": "system",
                      "content": "Recommend one robust Playwright locator. Keep the answer short and do not invent elements."
                    },
                    {
                      "role": "user",
                      "content": %s
                    }
                  ],
                  "temperature": 0.1
                }
                """.formatted(escape(TestConfig.aiModel()), jsonString(prompt));
    }

    private static String jsonString(String value) {
        return "\"" + escape(value) + "\"";
    }

    private static String escape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
