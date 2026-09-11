package com.example.playwright.healing;

import com.microsoft.playwright.Playwright;
import com.sun.net.httpserver.HttpServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import static org.junit.jupiter.api.Assertions.*;

@Tag("browser")
class LlmBrowserAgentTest {
    /** Checks real MCP generation and a repaired click with scripted decisions, then rejects ambiguous and unusable matches. */
    @Test void validatesRealBrowserAndRepairsThroughRealMcp() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            byte[] html = "<button onclick=\"document.title='submitted'\">Submit</button>".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, html.length);
            exchange.getResponseBody().write(html);
            exchange.close();
        });
        server.start();
        try (var playwright = Playwright.create(); var browser = playwright.chromium().launch();
             var mcp = PlaywrightMcpClient.start()) {
            var page = browser.newPage();
            String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/";
            page.navigate(url);
            assertEquals(0, page.locator("#broken").count());
            String snapshot = mcp.callTool("browser_navigate", new ObjectMapper().writeValueAsString(Map.of("url", url)));
            AtomicReference<String> ref = new AtomicReference<>();
            AtomicReference<String> generated = new AtomicReference<>();
            AtomicInteger turn = new AtomicInteger();
            var decision = LlmHealingAgent.investigate(evidence -> switch (turn.incrementAndGet()) {
                case 1 -> new LlmHealingAgent.Step("find", "Submit", "Scripted inspection for integration test");
                case 2 -> new LlmHealingAgent.Step("generate", ref.get(), "Generate observed ref");
                default -> new LlmHealingAgent.Step("propose", generated.get(), "Validate generated expression");
            }, (name, args) -> {
                String result = mcp.callTool(name, args);
                if (name.equals("browser_find")) {
                    var matcher = Pattern.compile("\\[ref=([^\\]]+)]").matcher(result);
                    assertTrue(matcher.find(), result);
                    ref.set(matcher.group(1));
                }
                if (name.equals("browser_generate_locator")) generated.set(PlaywrightMcpClient.extractLocator(result));
                return result;
            }, expression -> LlmHealingAgent.validate(page, expression), snapshot, new ArrayList<>());
            decision.strategy().resolve(page).click();
            assertEquals("submitted", page.title());
            page.setContent("<button>Submit</button><button>Submit</button>");
            assertNotEquals("valid", LlmHealingAgent.validate(page, generated.get()));
            page.setContent("<button disabled>Submit</button>");
            assertNotEquals("valid", LlmHealingAgent.validate(page, generated.get()));
            page.setContent("<button hidden>Submit</button>");
            assertNotEquals("valid", LlmHealingAgent.validate(page, generated.get()));
        } finally { server.stop(0); }
    }
}
