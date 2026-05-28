package com.urlShortner.project.service;

import com.urlShortner.project.dto.AnalyticsResponse;
import com.urlShortner.project.entity.Analytics;
import com.urlShortner.project.exception.UrlExpiredException;
import com.urlShortner.project.exception.UrlNotFoundException;
import com.urlShortner.project.repository.AnalyticsRepository;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.urlShortner.project.dto.ShortenUrlRequest;
import com.urlShortner.project.entity.Url;
import com.urlShortner.project.repository.UrlRepository;
import com.urlShortner.project.util.Base62Generator;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository urlRepository;

    public Url shortenUrl(ShortenUrlRequest request) {

        validateUrl(request.getUrl());

        String shortCode;

        Optional<Url> existingUrl =
                urlRepository.findByOriginalUrl(request.getUrl());

        if (existingUrl.isPresent()) {
            return existingUrl.get();
        }

        if (request.getCustomAlias() != null &&
                !request.getCustomAlias().isBlank()) {

            if (urlRepository.existsByShortCode(
                    request.getCustomAlias()
            )) {
                throw new RuntimeException(
                        "Alias already exists"
                );
            }
            shortCode = request.getCustomAlias();
        }
        else {
            do {
                shortCode =
                        Base62Generator.generateShortCode(6);
            } while (
                    urlRepository.existsByShortCode(shortCode)
            );
        }
        Url url = Url.builder()
                .shortCode(shortCode)
                .originalUrl(request.getUrl())
                .createdAt(LocalDateTime.now())
                .clickCount(0L)
                .expiresAt(
                        request.getExpiryDays() != null
                                ? LocalDateTime.now()
                                .plusDays(request.getExpiryDays())
                                : null
                )
                .build();
        return urlRepository.save(url);
    }

    private void validateUrl(String url) {

        try {

            URI uri = new URI(url);

            String scheme = uri.getScheme();

            if (scheme == null ||
                    (!scheme.equals("http") && !scheme.equals("https"))) {

                throw new RuntimeException("Invalid URL");
            }

        } catch (Exception e) {
            throw new RuntimeException("Malformed URL");
        }
    }

    private final RedisTemplate<String, Object> redisTemplate;

    public Url getOriginalUrl(String shortCode) {

        String cacheKey = "url:" + shortCode;

        Object cachedValue =
                redisTemplate.opsForValue().get(cacheKey);

        Url url;


        if (cachedValue != null) {

            url = Url.builder()
                    .shortCode(shortCode)
                    .originalUrl(cachedValue.toString())
                    .build();

        } else {

            url = urlRepository.findByShortCode(shortCode)
                    .orElseThrow(() ->
                            new UrlNotFoundException("URL not found")
                    );

            redisTemplate.opsForValue().set(
                    cacheKey,
                    url.getOriginalUrl(),
                    Duration.ofHours(24)
            );
        }
        if (url.getExpiresAt() != null &&
                LocalDateTime.now()
                        .isAfter(url.getExpiresAt())) {

            throw new UrlExpiredException("URL expired");
        }

        incrementAnalytics(shortCode);

        return url;
    }

    private void incrementAnalytics(String shortCode) {

        String hitsKey = "url:hits:" + shortCode;

        redisTemplate.opsForValue().increment(hitsKey);

        redisTemplate.opsForZSet()
                .incrementScore(
                        "trending_urls",
                        shortCode,
                        1
                );
    }

    public Set<Object> getTrendingUrls() {

        return redisTemplate.opsForZSet()
                .reverseRange(
                        "trending_urls",
                        0,
                        4
                );
    }

    private final AnalyticsRepository analyticsRepository;

    public AnalyticsResponse getAnalytics(String shortCode) {

        List<Analytics> analyticsList =
                analyticsRepository.findByShortCode(shortCode);

        Map<String, Long> browsers =
                analyticsList.stream()
                        .collect(Collectors.groupingBy(
                                Analytics::getBrowser,
                                Collectors.counting()
                        ));

        Map<String, Long> devices =
                analyticsList.stream()
                        .collect(Collectors.groupingBy(
                                Analytics::getDevice,
                                Collectors.counting()
                        ));

        List<String> recentIps =
                analyticsList.stream()
                        .limit(5)
                        .map(Analytics::getIp)
                        .toList();

        return AnalyticsResponse.builder()
                .totalClicks((long) analyticsList.size())
                .topBrowsers(browsers)
                .topDevices(devices)
                .recentIps(recentIps)
                .build();
    }

    @Async
    public void processAnalytics(
            String shortCode,
            String browser,
            String device,
            String ip,
            String referrer
    ) {

        Analytics analytics = Analytics.builder()
                .shortCode(shortCode)
                .timestamp(LocalDateTime.now())
                .browser(browser)
                .device(device)
                .ip(ip)
                .referrer(referrer)
                .build();

        analyticsRepository.save(analytics);
    }

    public boolean isRateLimited(String ip) {

        String key = "rate_limit:" + ip;

        Long requests =
                redisTemplate.opsForValue().increment(key);

        if (requests == 1) {

            redisTemplate.expire(
                    key,
                    Duration.ofSeconds(1)
            );
        }

        return requests > 10;
    }

}