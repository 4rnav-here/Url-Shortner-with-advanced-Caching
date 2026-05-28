package com.urlShortner.project.repository;

import com.urlShortner.project.entity.Analytics;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AnalyticsRepository
        extends MongoRepository<Analytics, String> {

    List<Analytics> findByShortCode(String shortCode);
}