package com.example.admin.websocket;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationWebSocketTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void websocketRejectsInvalidToken() {
        StandardWebSocketClient client = new StandardWebSocketClient();
        String url = "ws://localhost:" + port + "/ws/notification?token=invalid";

        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () ->
                client.execute(new NoopHandler(), new WebSocketHttpHeaders(), java.net.URI.create(url))
                        .get(3, TimeUnit.SECONDS)
        );
    }

    @Test
    void websocketInitContainsUnreadAfterOfflineAnnounce() throws Exception {
        String adminToken = login("admin", "admin123");

        String username = unique("ws_user");
        createUser(adminToken, username, "pwd123");
        String userToken = login(username, "pwd123");

        String title = "notice_" + UUID.randomUUID();

        // User is offline now; announce should store in DB.
        mockMvc.perform(post("/api/notifications/announce")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + title + "\",\"content\":\"content\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        BlockingQueue<String> messages = new LinkedBlockingQueue<>();
        TextWebSocketHandler handler = new TextWebSocketHandler() {
            @Override
            protected void handleTextMessage(WebSocketSession session, TextMessage message) {
                messages.add(message.getPayload());
            }
        };

        StandardWebSocketClient client = new StandardWebSocketClient();
        String url = "ws://localhost:" + port + "/ws/notification?token=" + userToken;
        WebSocketSession session = client.execute(handler, new WebSocketHttpHeaders(), java.net.URI.create(url))
                .get(5, TimeUnit.SECONDS);

        try {
            String first = messages.poll(5, TimeUnit.SECONDS);
            org.junit.jupiter.api.Assertions.assertNotNull(first);

            JsonNode json = objectMapper.readTree(first);
            org.junit.jupiter.api.Assertions.assertEquals("init", json.at("/type").asText());

            boolean found = false;
            for (JsonNode item : json.at("/data")) {
                if (title.equals(item.at("/title").asText())) {
                    found = true;
                    break;
                }
            }
            org.junit.jupiter.api.Assertions.assertTrue(found);
        } finally {
            try {
                session.close();
            } catch (Exception ignored) {
                // ignore
            }
        }
    }

    private void createUser(String token, String username, String password) throws Exception {
        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\",\"nickname\":\"" + username + "\",\"status\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    private String login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.at("/data/token").asText();
    }

    private String unique(String prefix) {
        return prefix + "_" + UUID.randomUUID();
    }

    private static class NoopHandler extends TextWebSocketHandler {}
}

