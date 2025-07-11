package com.mdasim.urlshortener.controller;

import com.mdasim.urlshortener.dto.CreateUrlRequest;
import com.mdasim.urlshortener.dto.ShortUrlResponse;
import com.mdasim.urlshortener.entity.ShortUrl;
import com.mdasim.urlshortener.service.ShortUrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/url")
@CrossOrigin(origins = "*", allowCredentials = "false")
 // Configure properly for production
public class ShortUrlController {

    @Autowired
    private ShortUrlService service;

    @PostMapping("/create")
    public ResponseEntity<ShortUrlResponse> createShortUrl(@Valid @RequestBody CreateUrlRequest request) {
        try {
            ShortUrl shortUrl = service.createShortUrl(request.getOriginalUrl());
            ShortUrlResponse response = new ShortUrlResponse(
                    shortUrl.getId(),
                    shortUrl.getOriginalUrl(),
                    shortUrl.getShortCode(),
                    shortUrl.getHitCount(),
                    shortUrl.getCreatedAt()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{code}")
    public RedirectView redirect(@PathVariable String code) {
        try {
            ShortUrl shortUrl = service.getOriginalUrl(code);
            return new RedirectView(shortUrl.getOriginalUrl());
        } catch (RuntimeException e) {
            return new RedirectView("/error/404");
        }
    }

    @GetMapping("/{code}/info")
    public ResponseEntity<ShortUrlResponse> getUrlInfo(@PathVariable String code) {
        try {
            ShortUrl shortUrl = service.getUrlInfo(code);
            ShortUrlResponse response = new ShortUrlResponse(
                    shortUrl.getId(),
                    shortUrl.getOriginalUrl(),
                    shortUrl.getShortCode(),
                    shortUrl.getHitCount(),
                    shortUrl.getCreatedAt()
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<ShortUrlResponse>> getAllUrls() {
        List<ShortUrl> urls = service.getAllUrls();
        List<ShortUrlResponse> responses = urls.stream()
                .map(url -> new ShortUrlResponse(
                        url.getId(),
                        url.getOriginalUrl(),
                        url.getShortCode(),
                        url.getHitCount(),
                        url.getCreatedAt()
                ))
                .toList();
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<Void> deleteUrl(@PathVariable String code) {
        try {
            service.deleteUrl(code);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{code}/redirect")
    public ResponseEntity<String> fetchOriginalUrl(@PathVariable String code) {
        ShortUrl shortUrl = service.getOriginalUrl(code);
        return ResponseEntity.ok(shortUrl.getOriginalUrl());
    }

}