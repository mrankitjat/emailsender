package com.example.recruitment.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine Caching configuration for Google Drive metadata and file content.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String GOOGLE_DRIVE_CACHE = "googleDriveCache";

    @Value("${email.cache-minutes:0}")
    private long cacheMinutes;

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(GOOGLE_DRIVE_CACHE);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(cacheMinutes, TimeUnit.MINUTES)
                .maximumSize(500)
                .recordStats());
        return cacheManager;
    }
}
