package com.urlShortner.project.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class AnalyticsResponse {

    private Long totalClicks;

    private Map<String, Long> topBrowsers;

    private Map<String, Long> topDevices;

    private List<String> recentIps;
}