package com.example.playwright.healing;

import com.example.playwright.config.TestConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PlaywrightMcpClient implements AutoCloseable {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Pattern REF_PATTERN = Pattern.compile("\\[ref=([^\\]]+)]");
    private static final Pattern LOCATOR_RESULT_PATTERN = Pattern.compile("(?m)^### Result\\R(.+?)\\s*$", Pattern.DOTALL);

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final AtomicInteger ids = new AtomicInteger();
    private final Process process;
    private final URI endpoint;
    private final String sessionId;

    private PlaywrightMcpClient(Process process, URI endpoint, String sessionId) {
        this.process = process;
        this.endpoint = endpoint;
        this.sessionId = sessionId;
    }

    public static PlaywrightMcpClient start() {
        int port = availablePort();
        Process process = startServer(port);
        URI endpoint = URI.create("http://localhost:" + port + "/mcp");
        PlaywrightMcpClient client = initialize(process, endpoint);
        Runtime.getRuntime().addShutdownHook(new Thread(client::close));
        return client;
    }

    public Optional<McpLocatorRecommendation> generateLocator(String pageUrl, ElementDefinition definition) {
        try {
            callTool("browser_navigate", """
                    {"url":%s}
                    """.formatted(jsonString(pageUrl)));

            String targetRef = findTargetRef(definition).orElse("");
            if (targetRef.isBlank()) {
                return Optional.empty();
            }

            String locatorOutput = callTool("browser_generate_locator", """
                    {"target":%s,"element":%s}
                    """.formatted(jsonString(targetRef), jsonString(definition.logicalName())));
            String locatorExpression = extractLocator(locatorOutput);
            if (locatorExpression.isBlank()) {
                return Optional.empty();
            }

            return Optional.of(new McpLocatorRecommendation(
                    targetRef,
                    locatorExpression,
                    locatorOutput,
                    "Official Playwright MCP browser_generate_locator"
            ));
        } catch (IOException e) {
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    private Optional<String> findTargetRef(ElementDefinition definition) throws IOException, InterruptedException {
        List<String> searchTerms = new ArrayList<>();
        searchTerms.addAll(definition.hints());
        searchTerms.add(definition.logicalName());

        for (String term : searchTerms) {
            if (term == null || term.isBlank()) {
                continue;
            }
            String result = callTool("browser_find", """
                    {"text":%s}
                    """.formatted(jsonString(term)));
            Optional<String> ref = bestRef(result, definition);
            if (ref.isPresent()) {
                return ref;
            }
        }
        return Optional.empty();
    }

    private Optional<String> bestRef(String browserFindOutput, ElementDefinition definition) {
        String lowerName = definition.logicalName().toLowerCase();
        for (String line : browserFindOutput.lines().toList()) {
            String lowerLine = line.toLowerCase();
            boolean likelyTarget = lowerLine.contains("button")
                    || lowerLine.contains("textbox")
                    || lowerLine.contains("link")
                    || lowerLine.contains("heading")
                    || lowerLine.contains("text:")
                    || lowerLine.contains("checkbox")
                    || lowerLine.contains("combobox");
            boolean matchesIntent = definition.hints().stream()
                    .anyMatch(hint -> !hint.isBlank() && lowerLine.contains(hint.toLowerCase()))
                    || lowerLine.contains(lowerName);
            if (likelyTarget && matchesIntent) {
                Matcher matcher = REF_PATTERN.matcher(line);
                if (matcher.find()) {
                    return Optional.of(matcher.group(1));
                }
            }
        }
        return Optional.empty();
    }

    private String callTool(String name, String argumentsJson) throws IOException, InterruptedException {
        return textContent(rpc("""
                {"jsonrpc":"2.0","id":%d,"method":"tools/call","params":{"name":%s,"arguments":%s}}
                """.formatted(ids.incrementAndGet(), jsonString(name), argumentsJson)));
    }

    private JsonNode rpc(String body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(endpoint)
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .header("mcp-session-id", sessionId)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return parseSseJson(httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body());
    }

    private static PlaywrightMcpClient initialize(Process process, URI endpoint) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
        for (int attempt = 0; attempt < 60; attempt++) {
            try {
                String initializeBody = """
                        {"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-06-18","capabilities":{},"clientInfo":{"name":"playwright-java-healing","version":"1.0.0"}}}
                        """;
                HttpRequest request = HttpRequest.newBuilder(endpoint)
                        .timeout(Duration.ofSeconds(5))
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json, text/event-stream")
                        .POST(HttpRequest.BodyPublishers.ofString(initializeBody))
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String sessionId = response.headers().firstValue("mcp-session-id")
                        .orElseThrow(() -> new IllegalStateException("Playwright MCP did not return mcp-session-id"));
                notifyInitialized(httpClient, endpoint, sessionId);
                return new PlaywrightMcpClient(process, endpoint, sessionId);
            } catch (IOException | InterruptedException | RuntimeException e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrupted while starting Playwright MCP", e);
                }
                sleep();
            }
        }
        process.destroyForcibly();
        throw new IllegalStateException("Playwright MCP server did not become ready at " + endpoint);
    }

    private static void notifyInitialized(HttpClient httpClient, URI endpoint, String sessionId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(endpoint)
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .header("mcp-session-id", sessionId)
                .POST(HttpRequest.BodyPublishers.ofString("{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\"}"))
                .build();
        httpClient.send(request, HttpResponse.BodyHandlers.discarding());
    }

    private static Process startServer(int port) {
        try {
            Process process = new ProcessBuilder(serverCommand(port))
                    .redirectErrorStream(true)
                    .start();
            Thread outputDrainer = new Thread(() -> drain(process), "playwright-mcp-output");
            outputDrainer.setDaemon(true);
            outputDrainer.start();
            return process;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to start Playwright MCP server", e);
        }
    }

    private static List<String> serverCommand(int port) {
        List<String> command = new ArrayList<>();
        command.add(TestConfig.playwrightMcpCommand());
        if ("npx".equals(TestConfig.playwrightMcpCommand())) {
            command.add("-y");
        }
        command.add(TestConfig.playwrightMcpPackage());
        command.add("--headless");
        command.add("--caps=testing");
        command.add("--port");
        command.add(String.valueOf(port));
        return command;
    }

    private static void drain(Process process) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            while (reader.readLine() != null) {
                // Keep the child process stdout pipe from blocking.
            }
        } catch (IOException ignored) {
        }
    }

    private static int availablePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to allocate Playwright MCP port", e);
        }
    }

    private static String textContent(JsonNode response) {
        JsonNode content = response.path("result").path("content");
        if (content.isArray() && !content.isEmpty()) {
            return content.get(0).path("text").asText("");
        }
        return "";
    }

    private static JsonNode parseSseJson(String body) throws IOException {
        StringBuilder data = new StringBuilder();
        for (String line : body.lines().toList()) {
            if (line.startsWith("data:")) {
                data.append(line.substring("data:".length()).trim());
            }
        }
        String json = data.isEmpty() ? body : data.toString();
        return JSON.readTree(json);
    }

    private static String extractLocator(String locatorOutput) {
        Matcher matcher = LOCATOR_RESULT_PATTERN.matcher(locatorOutput);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return locatorOutput.trim();
    }

    private static String jsonString(String value) {
        try {
            return JSON.writeValueAsString(value);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not encode JSON string", e);
        }
    }

    private static void sleep() {
        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void close() {
        process.destroy();
        try {
            if (!process.waitFor(2, TimeUnit.SECONDS)) {
                process.destroyForcibly();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
        }
    }
}
