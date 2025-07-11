package com.mdasim.urlshortener.service;

import com.mdasim.urlshortener.entity.ShortUrl;
import com.mdasim.urlshortener.exception.InvalidUrlException;
import com.mdasim.urlshortener.exception.UrlNotFoundException;
import com.mdasim.urlshortener.repository.ShortUrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ShortUrlService {

    @Autowired
    private ShortUrlRepository repository;

    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int SHORT_CODE_LENGTH = 6;
    private static final int MAX_RETRIES = 5;
    private final SecureRandom random = new SecureRandom();

    public ShortUrl createShortUrl(String originalUrl) {
        validateUrl(originalUrl);

        // Check if URL already exists
        Optional<ShortUrl> existingUrl = repository.findByOriginalUrl(originalUrl);
        if (existingUrl.isPresent()) {
            return existingUrl.get();
        }

        String shortCode = generateUniqueShortCode();
        ShortUrl shortUrl = new ShortUrl(originalUrl, shortCode);

        return repository.save(shortUrl);
    }

    @Transactional
    public ShortUrl getOriginalUrl(String code) {
        ShortUrl shortUrl = repository.findByShortCode(code)
                .orElseThrow(() -> new UrlNotFoundException("Short URL not found: " + code));

        shortUrl.incrementHitCount();
        return repository.save(shortUrl);
    }

    @Transactional(readOnly = true)
    public ShortUrl getUrlInfo(String code) {
        return repository.findByShortCode(code)
                .orElseThrow(() -> new UrlNotFoundException("Short URL not found: " + code));
    }

    @Transactional(readOnly = true)
    public List<ShortUrl> getAllUrls() {
        return repository.findAllOrderByHitCountDesc();
    }

    @Transactional
    public void deleteUrl(String code) {
        ShortUrl shortUrl = repository.findByShortCode(code)
                .orElseThrow(() -> new UrlNotFoundException("Short URL not found: " + code));
        repository.delete(shortUrl);
    }

    private void validateUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new InvalidUrlException("URL cannot be null or empty");
        }

        String trimmedUrl = url.trim();
        if (!trimmedUrl.startsWith("http://") && !trimmedUrl.startsWith("https://")) {
            // Add https:// if no protocol is specified
            trimmedUrl = "https://" + trimmedUrl;
        }

        try {
            new URL(trimmedUrl);
        } catch (MalformedURLException e) {
            throw new InvalidUrlException("Invalid URL format: " + url);
        }
    }

    private String generateUniqueShortCode() {
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            String shortCode = generateShortCode();
            if (!repository.existsByShortCode(shortCode)) {
                return shortCode;
            }
        }
        throw new RuntimeException("Unable to generate unique short code after " + MAX_RETRIES + " attempts");
    }

    private String generateShortCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < SHORT_CODE_LENGTH; i++) {
            code.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return code.toString();
    }
}