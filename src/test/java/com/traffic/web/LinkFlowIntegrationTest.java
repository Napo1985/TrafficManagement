package com.traffic.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(
        properties = {
            "server.port=0",
            "app.public-base-url=http://localhost:8081",
        })
@AutoConfigureMockMvc
class LinkFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createThenRedirect_happyPath() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/links")
                        .contentType("application/json")
                        .content(
                                """
                                {"targetUrl":"https://example.com/path?q=1","source":"test"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").isString())
                .andExpect(jsonPath("$.shortPath").isString())
                .andExpect(jsonPath("$.shortUrl").isString())
                .andReturn();

        JsonNode body = objectMapper.readTree(created.getResponse().getContentAsString());
        String slug = body.get("slug").asText();
        String shortPath = body.get("shortPath").asText();
        String shortUrl = body.get("shortUrl").asText();

        assertThat(slug).matches("[A-Za-z0-9]{6,8}");
        assertThat(shortPath).isEqualTo("/" + slug);
        assertThat(shortUrl).isEqualTo("http://localhost:8081" + shortPath);

        mockMvc.perform(get("/" + slug))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example.com/path?q=1"));
    }

    @Test
    void create_invalidUrl_returns400ProblemDetail() throws Exception {
        mockMvc.perform(post("/api/links")
                        .contentType("application/json")
                        .content("{\"targetUrl\":\"javascript:alert(1)\",\"source\":\"x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid URL"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void redirect_unknownSlug_returns404() throws Exception {
        mockMvc.perform(get("/Zz9zZz9")).andExpect(status().isNotFound());
    }

    @Test
    void create_blankTarget_returns400() throws Exception {
        mockMvc.perform(post("/api/links")
                        .contentType("application/json")
                        .content("{\"targetUrl\":\"   \",\"source\":\"x\"}"))
                .andExpect(status().isBadRequest());
    }
}
