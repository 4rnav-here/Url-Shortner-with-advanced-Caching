package com.urlShortner.project.config;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

import java.util.List;

@Configuration
@Getter
public class MongoConfig {

    @Value("${spring.data.mongodb.username}")
    private String username;

    @Value("${spring.data.mongodb.password}")
    private String password;

    @Value("${spring.data.mongodb.host}")
    private String host;

    @Value("${spring.data.mongodb.port}")
    private int port;

    @Value("${spring.data.mongodb.database}")
    private String database;

    @Value("${spring.data.mongodb.authentication-database}")
    private String authDatabase;

    @Bean
    public MongoClient mongoClient() {

        MongoCredential credential =
                MongoCredential.createCredential(
                        username,
                        authDatabase,
                        password.toCharArray()
                );

        ServerAddress serverAddress =
                new ServerAddress(host, port);

        MongoClientSettings settings =
                MongoClientSettings.builder()
                        .credential(credential)
                        .applyToClusterSettings(
                                builder -> builder.hosts(List.of(serverAddress))
                        )
                        .build();

        return MongoClients.create(settings);
    }

    @Bean
    public MongoDatabaseFactory mongoDatabaseFactory(
            MongoClient mongoClient
    ) {

        return new SimpleMongoClientDatabaseFactory(
                mongoClient,
                database
        );
    }
}