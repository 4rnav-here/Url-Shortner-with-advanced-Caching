package com.urlShortner.project.controller;

import com.urlShortner.project.dto.AnalyticsResponse;
import com.urlShortner.project.dto.ShortenUrlRequest;
import com.urlShortner.project.dto.ShortenUrlResponse;
import com.urlShortner.project.entity.Url;
import com.urlShortner.project.service.UrlService;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.connector.Request;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import jakarta.servlet.http.HttpServletRequest;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
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
            @PathVariable String shortCode,
            HttpServletRequest request
    ) {

        String userAgent = request.getHeader("User-Agent");

        String referrer = request.getHeader("Referer");

        String ip = request.getRemoteAddr();

        if (urlService.isRateLimited(ip)) {

            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .build();
        }

        Url url = urlService.getOriginalUrl(shortCode);

        String browser = "Unknown";

        if (userAgent != null) {

            if (userAgent.contains("Chrome")) {
                browser = "Chrome";
            } else if (userAgent.contains("Firefox")) {
                browser = "Firefox";
            }
        }

        String device =
                userAgent != null &&
                        userAgent.contains("Mobile")
                        ? "Mobile"
                        : "Desktop";

        urlService.processAnalytics(
                shortCode,
                browser,
                device,
                ip,
                referrer
        );

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(url.getOriginalUrl()))
                .build();
    }

    @GetMapping("/api/url/trending")
    public Set<Object> getTrendingUrls() {

        return urlService.getTrendingUrls();
    }

    @GetMapping("/analytics/{code}")
    public AnalyticsResponse getAnalytics(
            @PathVariable String code
    ) {

        return urlService.getAnalytics(code);
    }

    private final MongoTemplate mongoTemplate;

    private final RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/health")
    public Map<String, String> health() {

        Map<String, String> response =
                new HashMap<>();

        try {

            mongoTemplate.getDb()
                    .runCommand(new org.bson.Document("ping", 1));

            response.put("mongo", "UP");

        } catch (Exception e) {

            response.put("mongo", "DOWN");
        }

        try {

            redisTemplate.execute((RedisCallback<String>) connection -> connection.ping());

            response.put("redis", "UP");

        } catch (Exception e) {

            response.put("redis", "DOWN");
        }

        return response;
    }
}