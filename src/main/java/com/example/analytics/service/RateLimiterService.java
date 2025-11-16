package com.example.analytics.service;

import io.github.bucket4j.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class RateLimiterService {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    private Bucket createBucket() {
        return Bucket4j.builder()
                .addLimit(Bandwidth.simple(100, Duration.ofMinutes(1)))  
                .build();
    }

    public boolean allowRequest(String apiKey) {
        Bucket bucket = cache.computeIfAbsent(apiKey, k -> createBucket());
        return bucket.tryConsume(1);
    }
}
