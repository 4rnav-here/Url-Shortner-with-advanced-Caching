package com.urlShortner.project.repository;

import com.urlShortner.project.entity.Url;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UrlRepository extends MongoRepository<Url, String> {
    Optional<Url> findByShortCode(String shortCode);

    boolean existsByShortCode(String shortCode);
}
