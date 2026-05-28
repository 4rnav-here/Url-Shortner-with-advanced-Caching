package com.urlShortner.project.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "analytics")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Analytics {

    @Id
    private String id;

    private String shortCode;

    private LocalDateTime timestamp;

    private String browser;

    private String device;

    private String ip;

    private String referrer;
}