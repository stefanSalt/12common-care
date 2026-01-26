package com.example.admin.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MessageModuleTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createWithoutTokenReturns401Json() throws Exception {
        mockMvc.perform(post("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"t\",\"content\":\"c\",\"contactEmail\":\"a@b.com\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void userCreatesAdminRepliesUserCanSeeReplyAndGetsNotification() throws Exception {
        String adminToken = login("admin", "admin123");

        createUser(adminToken, "alice_msg", "alice123");
        String aliceToken = login("alice_msg", "alice123");

        long messageId = createMessage(aliceToken, "功能建议", "希望增加留言功能", "alice@example.com");

        // Normal user cannot list all messages or reply.
        mockMvc.perform(get("/api/messages")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));

        mockMvc.perform(put("/api/messages/" + messageId + "/reply")
                        .header("Authorization", "Bearer " + aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"replyContent\":\"ok\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));

        // Admin can list and reply.
        long found = findMessageIdByTitle(adminToken, "功能建议");
        org.junit.jupiter.api.Assertions.assertEquals(messageId, found);

        mockMvc.perform(put("/api/messages/" + messageId + "/reply")
                        .header("Authorization", "Bearer " + adminToken)
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"replyContent\":\"已收到，我们会尽快处理。\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value(1))
                .andExpect(jsonPath("$.data.replyContent").value("已收到，我们会尽快处理。"));

        // User can see reply in my list.
        JsonNode myAfter = listMyMessages(aliceToken, 1, 50);
        boolean foundReplied = false;
        for (JsonNode item : myAfter.at("/data/records")) {
            if (item.at("/id").asLong() == messageId) {
                foundReplied = item.at("/status").asInt() == 1
                        && "已收到，我们会尽快处理。".equals(item.at("/replyContent").asText());
                break;
            }
        }
        org.junit.jupiter.api.Assertions.assertTrue(foundReplied);

        // User received a system notification after reply.
        boolean hasNotif = hasNotificationTitle(aliceToken, "留言已回复");
        org.junit.jupiter.api.Assertions.assertTrue(hasNotif);
    }

    private long createMessage(String token, String title, String content, String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/messages")
                        .header("Authorization", "Bearer " + token)
                        .characterEncoding("UTF-8")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + title + "\",\"content\":\"" + content + "\",\"contactEmail\":\"" + email + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        return json.at("/data/id").asLong();
    }

    private JsonNode listMyMessages(String token, int current, int size) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/messages/my")
                        .header("Authorization", "Bearer " + token)
                        .param("current", String.valueOf(current))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private long findMessageIdByTitle(String adminToken, String title) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/messages")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("current", "1")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        for (JsonNode item : json.at("/data/records")) {
            if (title.equals(item.at("/title").asText())) {
                return item.at("/id").asLong();
            }
        }
        throw new AssertionError("message not found by title: " + title + ", list=" + json.toString());
    }

    private boolean hasNotificationTitle(String token, String title) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + token)
                        .param("current", "1")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        for (JsonNode item : json.at("/data/records")) {
            if (title.equals(item.at("/title").asText())) {
                return true;
            }
        }
        return false;
    }

    private void createUser(String adminToken, String username, String password) throws Exception {
        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
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

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        return json.at("/data/token").asText();
    }
}
