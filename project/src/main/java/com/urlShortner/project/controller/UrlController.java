package com.urlShortner.project.controller;

import com.urlShortner.project.dto.ShortenUrlRequest;
import com.urlShortner.project.dto.ShortenUrlResponse;
import com.urlShortner.project.entity.Url;
import com.urlShortner.project.service.UrlService;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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

}
