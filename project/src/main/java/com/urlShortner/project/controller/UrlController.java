package com.urlShortner.project.controller;

import com.urlShortner.project.dto.ShortenUrlRequest;
import com.urlShortner.project.dto.ShortenUrlResponse;
import com.urlShortner.project.entity.Url;
import com.urlShortner.project.service.UrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Set;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;

    @PostMapping("/api/url/shorten")
    public ShortenUrlResponse shortenUrl(
            @RequestBody ShortenUrlRequest request
    ) {

        Url savedUrl = urlService.shortenUrl(request);

        return new ShortenUrlResponse(
                "http://localhost:8080/" + savedUrl.getShortCode()
        );
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(
            @PathVariable String shortCode) {

        Url url = urlService.getOriginalUrl(shortCode);

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(url.getOriginalUrl()))
                .build();
    }

    @GetMapping("/api/url/trending")
    public Set<Object> getTrendingUrls() {

        return urlService.getTrendingUrls();
    }
}