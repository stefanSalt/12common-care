package com.example.admin.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
class CrowdfundingModuleTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void crowdfundingEndToEnd() throws Exception {
        String adminToken = login("admin", "admin123");

        createUser(adminToken, "alice_cf", "alice123");
        String aliceToken = login("alice_cf", "alice123");

        // Alice creates a project (PENDING).
        String projectId = createProject(
                aliceToken,
                "p1",
                LocalDateTime.now().plusDays(2).withNano(0),
                100
        );

        // Public list should not include pending projects.
        JsonNode publicList = listPublic(1, 50);
        Assertions.assertFalse(containsTitle(publicList, "p1"));

        // Normal user cannot access admin list.
        mockMvc.perform(get("/api/crowdfunding")
                        .header("Authorization", "Bearer " + aliceToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));

        // Admin approves the project, now it appears in public list.
        reviewProject(adminToken, projectId, "APPROVE");
        publicList = listPublic(1, 50);
        Assertions.assertTrue(containsTitle(publicList, "p1"));

        // Public detail donations initially empty.
        JsonNode detail0 = publicDetail(projectId);
        Assertions.assertEquals(0, detail0.at("/data/latestDonations").size());

        // Alice donates multiple times, one anonymous.
        donate(aliceToken, projectId, 10, false);
        donate(aliceToken, projectId, 5, true);

        JsonNode detail1 = publicDetail(projectId);
        Assertions.assertEquals(2, detail1.at("/data/latestDonations").size());
        Assertions.assertEquals(15, detail1.at("/data/project/raisedAmount").asInt());

        String d0Name = detail1.at("/data/latestDonations/0/donorName").asText();
        String d1Name = detail1.at("/data/latestDonations/1/donorName").asText();
        Assertions.assertTrue(d0Name.equals("匿名") || d1Name.equals("匿名"), "d0Name=" + d0Name + ", d1Name=" + d1Name);

        // My donation records should include both donations.
        JsonNode myDonations = listMyDonations(aliceToken, 1, 50);
        Assertions.assertEquals(2, myDonations.at("/data/records").size());
        Assertions.assertEquals("p1", myDonations.at("/data/records/0/projectTitle").asText());

        // Admin donation records should include anonymous flag and username.
        JsonNode adminDonations = listAllDonations(adminToken, 1, 50, projectId);
        Assertions.assertEquals(2, adminDonations.at("/data/records").size());
        Assertions.assertEquals(1, adminDonations.at("/data/records/0/isAnonymous").asInt());
        Assertions.assertEquals("alice_cf", adminDonations.at("/data/records/0/username").asText());

        // Ended projects forbid donating (even if approved).
        String endedProjectId = createProject(
                aliceToken,
                "p2",
                LocalDateTime.now().minusDays(1).withNano(0),
                100
        );
        reviewProject(adminToken, endedProjectId, "APPROVE");
        mockMvc.perform(post("/api/crowdfunding/" + endedProjectId + "/donations")
                        .header("Authorization", "Bearer " + aliceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":1,\"anonymous\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));

        // REJECTED can be edited and submitted again.
        String project3Id = createProject(
                aliceToken,
                "p3",
                LocalDateTime.now().plusDays(2).withNano(0),
                100
        );
        reviewProject(adminToken, project3Id, "REJECT");

        updateProject(aliceToken, project3Id, "p3_new");
        submitProject(aliceToken, project3Id);
        reviewProject(adminToken, project3Id, "APPROVE");

        JsonNode publicList2 = listPublic(1, 50);
        Assertions.assertTrue(containsTitle(publicList2, "p3_new"));
    }

    private boolean containsTitle(JsonNode listResponse, String title) {
        for (JsonNode item : listResponse.at("/data/records")) {
            if (title.equals(item.at("/title").asText())) {
                return true;
            }
        }
        return false;
    }

    private JsonNode listPublic(int current, int size) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/crowdfunding/public")
                        .param("current", String.valueOf(current))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private JsonNode publicDetail(String id) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/crowdfunding/" + id + "/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private JsonNode listMyDonations(String token, int current, int size) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/crowdfunding/my/donations")
                        .header("Authorization", "Bearer " + token)
                        .param("current", String.valueOf(current))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private JsonNode listAllDonations(String token, int current, int size, String projectId) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/crowdfunding/donations")
                        .header("Authorization", "Bearer " + token)
                        .param("current", String.valueOf(current))
                        .param("size", String.valueOf(size))
                        .param("projectId", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private String createProject(String token, String title, LocalDateTime endTime, int targetAmount) throws Exception {
        String et = endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        String payload = "{"
                + "\"title\":\"" + title + "\","
                + "\"coverFileId\":5001,"
                + "\"content\":\"<p>hi</p><img src=/api/files/5001/download>\","
                + "\"targetAmount\":" + targetAmount + ","
                + "\"endTime\":\"" + et + "\""
                + "}";

        MvcResult result = mockMvc.perform(post("/api/crowdfunding")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.at("/data/id").asText();
    }

    private void updateProject(String token, String projectId, String newTitle) throws Exception {
        mockMvc.perform(put("/api/crowdfunding/" + projectId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + newTitle + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    private void submitProject(String token, String projectId) throws Exception {
        mockMvc.perform(put("/api/crowdfunding/" + projectId + "/submit")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    private void donate(String token, String projectId, int amount, boolean anonymous) throws Exception {
        mockMvc.perform(post("/api/crowdfunding/" + projectId + "/donations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":" + amount + ",\"anonymous\":" + anonymous + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    private void reviewProject(String adminToken, String projectId, String action) throws Exception {
        mockMvc.perform(put("/api/crowdfunding/" + projectId + "/review")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"" + action + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
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
