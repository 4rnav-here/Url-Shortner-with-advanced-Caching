package com.urlShortner.project.service;

import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.Set;

import com.urlShortner.project.dto.ShortenUrlRequest;
import com.urlShortner.project.entity.Url;
import com.urlShortner.project.repository.UrlRepository;
import com.urlShortner.project.util.Base62Generator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;

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

        do {
            shortCode = Base62Generator.generateShortCode(6);
        }
        while (urlRepository.existsByShortCode(shortCode));

        Url url = Url.builder()
                .shortCode(shortCode)
                .originalUrl(request.getUrl())
                .createdAt(LocalDateTime.now())
                .clickCount(0L)
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
                            new RuntimeException("URL not found")
                    );

            redisTemplate.opsForValue().set(
                    cacheKey,
                    url.getOriginalUrl(),
                    Duration.ofHours(24)
            );
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



}