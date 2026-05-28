package com.urlShortner.project.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShortenUrlRequest {

    private String url;

    private Integer expiryDays;

    private String customAlias;
}
