package com.example.admin.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.admin.entity.SysFile;
import com.example.admin.mapper.SysFileMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FileModuleTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SysFileMapper fileMapper;

    @Test
    void fileDownloadAccessControlWorks() throws Exception {
        String adminToken = login("admin", "admin123");

        // Upload a PRIVATE file as admin
        long privateFileId = uploadFile(adminToken, "hello.txt", "text/plain", "hello", "PRIVATE");

        // Anonymous download should be rejected for PRIVATE
        mockMvc.perform(get("/api/files/" + privateFileId + "/download"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        // Create a normal user and login
        String bobUsername = unique("bob");
        createUser(adminToken, bobUsername, "bob123");
        String bobToken = login(bobUsername, "bob123");

        // Authenticated but not owner: forbidden
        mockMvc.perform(get("/api/files/" + privateFileId + "/download")
                        .header("Authorization", "Bearer " + bobToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));

        // Owner can download
        MvcResult ownerDownload = mockMvc.perform(get("/api/files/" + privateFileId + "/download")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();
        String ownerBody = ownerDownload.getResponse().getContentAsString();
        org.junit.jupiter.api.Assertions.assertEquals("hello", ownerBody);

        // Upload a PUBLIC file and verify anonymous can download
        long publicFileId = uploadFile(adminToken, "public.txt", "text/plain", "world", "PUBLIC");
        MvcResult publicDownload = mockMvc.perform(get("/api/files/" + publicFileId + "/download"))
                .andExpect(status().isOk())
                .andReturn();
        String publicBody = publicDownload.getResponse().getContentAsString();
        org.junit.jupiter.api.Assertions.assertEquals("world", publicBody);
    }

    @Test
    void fileListIsUserIsolatedAndPathsUnique() throws Exception {
        String adminToken = login("admin", "admin123");

        long fileId1 = uploadFile(adminToken, "same.txt", "text/plain", "x", "PRIVATE");
        long fileId2 = uploadFile(adminToken, "same.txt", "text/plain", "x", "PRIVATE");

        SysFile f1 = fileMapper.selectById(fileId1);
        SysFile f2 = fileMapper.selectById(fileId2);
        org.junit.jupiter.api.Assertions.assertNotNull(f1);
        org.junit.jupiter.api.Assertions.assertNotNull(f2);
        org.junit.jupiter.api.Assertions.assertNotEquals(f1.getPath(), f2.getPath());

        String aliceUsername = unique("alice");
        createUser(adminToken, aliceUsername, "alice123");
        String aliceToken = login(aliceUsername, "alice123");
        long aliceFileId = uploadFile(aliceToken, "alice.txt", "text/plain", "alice", "PRIVATE");

        JsonNode adminList = listMyFiles(adminToken, 1, 200);
        org.junit.jupiter.api.Assertions.assertFalse(containsId(adminList.at("/data/records"), aliceFileId));

        JsonNode aliceList = listMyFiles(aliceToken, 1, 200);
        org.junit.jupiter.api.Assertions.assertFalse(containsId(aliceList.at("/data/records"), fileId1));
        org.junit.jupiter.api.Assertions.assertFalse(containsId(aliceList.at("/data/records"), fileId2));
    }

    private long uploadFile(String token, String filename, String contentType, String content, String visibility) throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                filename,
                contentType,
                content.getBytes()
        );

        MvcResult result = mockMvc.perform(multipart("/api/files/upload")
                        .file(file)
                        .param("visibility", visibility)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.at("/data/id").asLong();
    }

    private JsonNode listMyFiles(String token, int current, int size) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/files")
                        .header("Authorization", "Bearer " + token)
                        .param("current", String.valueOf(current))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private boolean containsId(JsonNode array, long id) {
        if (array == null || !array.isArray()) {
            return false;
        }
        for (JsonNode item : array) {
            if (item.at("/id").asLong() == id) {
                return true;
            }
        }
        return false;
    }

    private long createUser(String adminToken, String username, String password) throws Exception {
        MvcResult created = mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\",\"nickname\":\"" + username + "\",\"status\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();

        return objectMapper.readTree(created.getResponse().getContentAsString()).at("/data/id").asLong();
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
}
