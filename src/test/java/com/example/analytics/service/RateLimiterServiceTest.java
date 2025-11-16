package com.example.analytics.service;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class RateLimiterServiceTest {

    private RateLimiterService rateLimiterService;

    @BeforeEach
    void setUp() {
        rateLimiterService = new RateLimiterService();
    }

    @Test
    void testAllowRequest_FirstRequestShouldPass() {
        String apiKey = "key123";

        boolean result = rateLimiterService.allowRequest(apiKey);

        assertTrue(result, "First request must be allowed");
    }

    @Test
    void testAllowRequest_ShouldBlockAfterLimitExceeded() {
        String apiKey = "key123";

        
        for (int i = 0; i < 100; i++) {
            assertTrue(rateLimiterService.allowRequest(apiKey));
        }

       
        boolean blocked = rateLimiterService.allowRequest(apiKey);

        assertFalse(blocked, "101st request should be blocked");
    }
}
