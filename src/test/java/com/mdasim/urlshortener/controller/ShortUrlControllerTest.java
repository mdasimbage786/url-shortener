// File: src/test/java/com/mdasim/urlshortener/controller/ShortUrlControllerTest.java
package com.mdasim.urlshortener.controller;

import com.mdasim.urlshortener.dto.CreateUrlRequest;
import com.mdasim.urlshortener.entity.ShortUrl;
import com.mdasim.urlshortener.service.ShortUrlService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShortUrlController.class)
class ShortUrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShortUrlService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void createShortUrl_ValidRequest_Success() throws Exception {
        // Given
        CreateUrlRequest request = new CreateUrlRequest();
        request.setOriginalUrl("https://example.com");

        ShortUrl shortUrl = new ShortUrl("https://example.com", "abc123");
        shortUrl.setId(1L);
        shortUrl.setCreatedAt(LocalDateTime.now());

        when(service.createShortUrl(anyString())).thenReturn(shortUrl);

        // When & Then
        mockMvc.perform(post("/api/url/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.originalUrl").value("https://example.com"))
                .andExpect(jsonPath("$.shortCode").value("abc123"));
    }

    @Test
    void redirect_ValidCode_Success() throws Exception {
        // Given
        ShortUrl shortUrl = new ShortUrl("https://example.com", "abc123");
        when(service.getOriginalUrl("abc123")).thenReturn(shortUrl);

        // When & Then
        mockMvc.perform(get("/api/url/abc123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("https://example.com"));
    }

    @Test
    @WithMockUser
    void getUrlInfo_ValidCode_Success() throws Exception {
        // Given
        ShortUrl shortUrl = new ShortUrl("https://example.com", "abc123");
        shortUrl.setId(1L);
        shortUrl.setHitCount(5);
        shortUrl.setCreatedAt(LocalDateTime.now());

        when(service.getUrlInfo("abc123")).thenReturn(shortUrl);

        // When & Then
        mockMvc.perform(get("/api/url/abc123/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalUrl").value("https://example.com"))
                .andExpect(jsonPath("$.shortCode").value("abc123"))
                .andExpect(jsonPath("$.hitCount").value(5));
    }

    @Test
    @WithMockUser
    void getAllUrls_Success() throws Exception {
        // Given
        ShortUrl url1 = new ShortUrl("https://example.com", "abc123");
        url1.setId(1L);
        url1.setCreatedAt(LocalDateTime.now());

        ShortUrl url2 = new ShortUrl("https://google.com", "def456");
        url2.setId(2L);
        url2.setCreatedAt(LocalDateTime.now());

        List<ShortUrl> urls = Arrays.asList(url1, url2);
        when(service.getAllUrls()).thenReturn(urls);

        // When & Then
        mockMvc.perform(get("/api/url/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].shortCode").value("abc123"))
                .andExpect(jsonPath("$[1].shortCode").value("def456"));
    }

    @Test
    @WithMockUser
    void createShortUrl_InvalidRequest_BadRequest() throws Exception {
        // Given
        CreateUrlRequest request = new CreateUrlRequest();
        request.setOriginalUrl(""); // Empty URL

        // When & Then
        mockMvc.perform(post("/api/url/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}