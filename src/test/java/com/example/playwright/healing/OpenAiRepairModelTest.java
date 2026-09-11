package com.example.playwright.healing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class OpenAiRepairModelTest {
    /** Tests provider HTTP failures, incomplete responses, missing output, and malformed JSON. */
    @Test void failsClosedOnProviderErrorsRefusalsAndIncompleteOutput() throws Exception {
        for (String payload : java.util.List.of("unauthorized secret-body", "{\"status\":\"incomplete\"}",
                "{\"status\":\"completed\",\"output\":[]}", "not json")) {
            HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/", exchange -> {
                byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(payload.startsWith("unauthorized") ? 401 : 200, bytes.length);
                exchange.getResponseBody().write(bytes);
                exchange.close();
            });
            server.start();
            try {
                var model = new OpenAiRepairModel("test-key", "test-model",
                        URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/"));
                var error = assertThrows(Exception.class, () -> model.next("test evidence"));
                assertFalse(error.getMessage().contains("secret-body"));
            } finally { server.stop(0); }
        }
    }

    /** Verifies bearer authentication, model selection, response storage, and strict schema against a mock server. */
    @Test void sendsAuthenticationAndStructuredSchema() throws Exception {
        AtomicReference<String> authorization = new AtomicReference<>();
        AtomicReference<String> body = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/responses", exchange -> {
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            var json = new ObjectMapper();
            var root = json.createObjectNode().put("status", "completed");
            root.putArray("output").addObject().putArray("content").addObject()
                    .put("type", "output_text").put("text", "{\"action\":\"abort\",\"value\":\"\",\"summary\":\"No target\"}");
            byte[] response = json.writeValueAsBytes(root);
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();
        try {
            var model = new OpenAiRepairModel("test-key", "test-model",
                    URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/responses"));
            assertEquals("abort", model.next("test evidence").action());
            assertEquals("Bearer test-key", authorization.get());
            var request = new ObjectMapper().readTree(body.get());
            assertTrue(request.path("text").path("format").path("strict").asBoolean());
            assertFalse(request.path("store").asBoolean());
            assertEquals("test-model", request.path("model").asText());
        } finally { server.stop(0); }
    }
}
