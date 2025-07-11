// File: src/test/java/com/mdasim/urlshortener/service/ShortUrlServiceTest.java
package com.mdasim.urlshortener.service;

import com.mdasim.urlshortener.entity.ShortUrl;
import com.mdasim.urlshortener.exception.InvalidUrlException;
import com.mdasim.urlshortener.exception.UrlNotFoundException;
import com.mdasim.urlshortener.repository.ShortUrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShortUrlServiceTest {

    @Mock
    private ShortUrlRepository repository;

    @InjectMocks
    private ShortUrlService service;

    private ShortUrl testUrl;

    @BeforeEach
    void setUp() {
        testUrl = new ShortUrl("https://example.com", "abc123");
        testUrl.setId(1L);
        testUrl.setCreatedAt(LocalDateTime.now());
        testUrl.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void createShortUrl_ValidUrl_Success() {
        // Given
        when(repository.findByOriginalUrl(anyString())).thenReturn(Optional.empty());
        when(repository.existsByShortCode(anyString())).thenReturn(false);
        when(repository.save(any(ShortUrl.class))).thenReturn(testUrl);

        // When
        ShortUrl result = service.createShortUrl("https://example.com");

        // Then
        assertNotNull(result);
        assertEquals("https://example.com", result.getOriginalUrl());
        assertEquals("abc123", result.getShortCode());
        verify(repository).save(any(ShortUrl.class));
    }

    @Test
    void createShortUrl_DuplicateUrl_ReturnExisting() {
        // Given
        when(repository.findByOriginalUrl("https://example.com")).thenReturn(Optional.of(testUrl));

        // When
        ShortUrl result = service.createShortUrl("https://example.com");

        // Then
        assertNotNull(result);
        assertEquals("https://example.com", result.getOriginalUrl());
        assertEquals("abc123", result.getShortCode());
        verify(repository, never()).save(any(ShortUrl.class));
    }

    @Test
    void createShortUrl_InvalidUrl_ThrowsException() {
        // Given
        String invalidUrl = "invalid-url";

        // When & Then
        assertThrows(InvalidUrlException.class, () -> service.createShortUrl(invalidUrl));
        verify(repository, never()).save(any(ShortUrl.class));
    }

    @Test
    void createShortUrl_NullUrl_ThrowsException() {
        // When & Then
        assertThrows(InvalidUrlException.class, () -> service.createShortUrl(null));
        verify(repository, never()).save(any(ShortUrl.class));
    }

    @Test
    void createShortUrl_EmptyUrl_ThrowsException() {
        // When & Then
        assertThrows(InvalidUrlException.class, () -> service.createShortUrl(""));
        verify(repository, never()).save(any(ShortUrl.class));
    }

    @Test
    void getOriginalUrl_ValidCode_Success() {
        // Given
        when(repository.findByShortCode("abc123")).thenReturn(Optional.of(testUrl));
        when(repository.save(any(ShortUrl.class))).thenReturn(testUrl);

        // When
        ShortUrl result = service.getOriginalUrl("abc123");

        // Then
        assertNotNull(result);
        assertEquals("https://example.com", result.getOriginalUrl());
        assertEquals("abc123", result.getShortCode());
        assertEquals(1, result.getHitCount()); // Hit count should be incremented
        verify(repository).save(any(ShortUrl.class));
    }

    @Test
    void getOriginalUrl_InvalidCode_ThrowsException() {
        // Given
        when(repository.findByShortCode("invalid")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UrlNotFoundException.class, () -> service.getOriginalUrl("invalid"));
        verify(repository, never()).save(any(ShortUrl.class));
    }

    @Test
    void getUrlInfo_ValidCode_Success() {
        // Given
        when(repository.findByShortCode("abc123")).thenReturn(Optional.of(testUrl));

        // When
        ShortUrl result = service.getUrlInfo("abc123");

        // Then
        assertNotNull(result);
        assertEquals("https://example.com", result.getOriginalUrl());
        assertEquals("abc123", result.getShortCode());
        assertEquals(0, result.getHitCount()); // Hit count should not be incremented
        verify(repository, never()).save(any(ShortUrl.class));
    }

    @Test
    void getUrlInfo_InvalidCode_ThrowsException() {
        // Given
        when(repository.findByShortCode("invalid")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UrlNotFoundException.class, () -> service.getUrlInfo("invalid"));
    }

    @Test
    void getAllUrls_Success() {
        // Given
        ShortUrl url1 = new ShortUrl("https://example.com", "abc123");
        ShortUrl url2 = new ShortUrl("https://google.com", "def456");
        List<ShortUrl> urls = Arrays.asList(url1, url2);

        when(repository.findAllOrderByHitCountDesc()).thenReturn(urls);

        // When
        List<ShortUrl> result = service.getAllUrls();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("abc123", result.get(0).getShortCode());
        assertEquals("def456", result.get(1).getShortCode());
    }

    @Test
    void deleteUrl_ValidCode_Success() {
        // Given
        when(repository.findByShortCode("abc123")).thenReturn(Optional.of(testUrl));
        doNothing().when(repository).delete(testUrl);

        // When
        assertDoesNotThrow(() -> service.deleteUrl("abc123"));

        // Then
        verify(repository).delete(testUrl);
    }

    @Test
    void deleteUrl_InvalidCode_ThrowsException() {
        // Given
        when(repository.findByShortCode("invalid")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UrlNotFoundException.class, () -> service.deleteUrl("invalid"));
        verify(repository, never()).delete(any(ShortUrl.class));
    }
}