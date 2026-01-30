package com.example.admin.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
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
class StoryCommentModuleTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void storyAndCommentEndToEnd() throws Exception {
        String adminToken = login("admin", "admin123");

        createUser(adminToken, "alice_story", "alice123");
        String aliceToken = login("alice_story", "alice123");

        // Admin creates a story.
        String storyId = createStory(adminToken, "s1", 5001, "<p>hi</p><img src=/api/files/5001/download>", 1);

        // Public list contains the story.
        JsonNode publicList = listPublicStories(1, 50);
        Assertions.assertTrue(containsTitle(publicList, "s1"));

        // Public detail returns story content.
        mockMvc.perform(get("/api/stories/" + storyId + "/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(storyId))
                .andExpect(jsonPath("$.data.content").exists());

        // Normal user cannot access admin list.
        mockMvc.perform(get("/api/stories").header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));

        // User posts a comment.
        String commentId = createComment(aliceToken, "STORY", storyId, "c1");

        // Public comment list contains it.
        JsonNode comments = listPublicComments("STORY", storyId, 1, 50);
        Assertions.assertTrue(containsComment(comments, "c1"));

        // My comments contains it.
        JsonNode myComments = listMyComments(aliceToken, 1, 50);
        Assertions.assertTrue(containsComment(myComments, "c1"));

        // User deletes own comment.
        mockMvc.perform(delete("/api/comments/" + commentId).header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        JsonNode myComments2 = listMyComments(aliceToken, 1, 50);
        Assertions.assertFalse(containsComment(myComments2, "c1"));
    }

    private boolean containsTitle(JsonNode listResponse, String title) {
        for (JsonNode item : listResponse.at("/data/records")) {
            if (title.equals(item.at("/title").asText())) {
                return true;
            }
        }
        return false;
    }

    private boolean containsComment(JsonNode listResponse, String content) {
        for (JsonNode item : listResponse.at("/data/records")) {
            if (content.equals(item.at("/content").asText())) {
                return true;
            }
        }
        return false;
    }

    private JsonNode listPublicStories(int current, int size) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/stories/public")
                        .param("current", String.valueOf(current))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private JsonNode listPublicComments(String targetType, String targetId, int current, int size) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/comments/public")
                        .param("targetType", targetType)
                        .param("targetId", targetId)
                        .param("current", String.valueOf(current))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private JsonNode listMyComments(String token, int current, int size) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/comments/my")
                        .header("Authorization", "Bearer " + token)
                        .param("current", String.valueOf(current))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private String createStory(String adminToken, String title, long coverFileId, String content, int enabled) throws Exception {
        String payload = "{"
                + "\"title\":\"" + title + "\","
                + "\"coverFileId\":" + coverFileId + ","
                + "\"content\":\"" + content.replace("\"", "\\\"") + "\","
                + "\"enabled\":" + enabled
                + "}";

        MvcResult result = mockMvc.perform(post("/api/stories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.at("/data/id").asText();
    }

    private String createComment(String token, String targetType, String targetId, String content) throws Exception {
        String payload = "{"
                + "\"targetType\":\"" + targetType + "\","
                + "\"targetId\":" + targetId + ","
                + "\"content\":\"" + content + "\""
                + "}";

        MvcResult result = mockMvc.perform(post("/api/comments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.at("/data/id").asText();
    }

    private void createUser(String adminToken, String username, String password) throws Exception {
        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password
                                + "\",\"nickname\":\"" + username + "\",\"status\":1}"))
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
}

