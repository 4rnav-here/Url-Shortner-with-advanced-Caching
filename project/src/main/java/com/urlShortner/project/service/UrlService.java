package com.urlShortner.project.service;

import com.urlShortner.project.dto.ShortenUrlRequest;
import com.urlShortner.project.entity.Url;
import com.urlShortner.project.repository.UrlRepository;
import com.urlShortner.project.util.Base62Generator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository urlRepository;

    public Url shortenUrl(ShortenUrlRequest request) {

        validateUrl(request.getUrl());

        String shortCode;

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
}