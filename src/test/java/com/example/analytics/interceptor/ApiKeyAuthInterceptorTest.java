package com.example.analytics.interceptor;

import com.example.analytics.entity.ApiKey;
import org.springframework.test.util.ReflectionTestUtils;
import com.example.analytics.repository.ApiKeyRepository;
import com.example.analytics.service.RateLimiterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApiKeyAuthInterceptorTest {

    private ApiKeyAuthInterceptor interceptor;
    private ApiKeyRepository apiKeyRepository;
    private RateLimiterService rateLimiterService;

    @BeforeEach
    void setup() {
        apiKeyRepository = Mockito.mock(ApiKeyRepository.class);
        rateLimiterService = Mockito.mock(RateLimiterService.class);

        interceptor = new ApiKeyAuthInterceptor();

        ReflectionTestUtils.setField(interceptor, "apiKeyRepository", apiKeyRepository);
        ReflectionTestUtils.setField(interceptor, "rateLimiterService", rateLimiterService);
    }

 
    @Test
    void testMissingApiKey() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(req, res, null);

        assertFalse(result);
        assertEquals(401, res.getStatus());
        assertEquals("Missing API Key", res.getErrorMessage());
    }

 
    @Test
    void testInvalidApiKey() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();

        req.addHeader("X-API-KEY", "invalid");

        when(apiKeyRepository.findByApiKey("invalid")).thenReturn(Optional.empty());

        boolean result = interceptor.preHandle(req, res, null);

        assertFalse(result);
        assertEquals(401, res.getStatus());
        assertEquals("Invalid API Key", res.getErrorMessage());
    }

 
    @Test
    void testInactiveApiKey() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();

        req.addHeader("X-API-KEY", "key123");

        ApiKey key = new ApiKey();
        key.setActive(false);

        when(apiKeyRepository.findByApiKey("key123")).thenReturn(Optional.of(key));

        boolean result = interceptor.preHandle(req, res, null);

        assertFalse(result);
        assertEquals(401, res.getStatus());
        assertEquals("Invalid API Key", res.getErrorMessage());
    }

 
    @Test
    void testExpiredApiKey() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();

        req.addHeader("X-API-KEY", "expiredKey");

        ApiKey key = new ApiKey();
        key.setActive(true);
        key.setExpiresAt(LocalDateTime.now().minusDays(1));  

        when(apiKeyRepository.findByApiKey("expiredKey")).thenReturn(Optional.of(key));

        boolean result = interceptor.preHandle(req, res, null);

        assertFalse(result);
        assertEquals(401, res.getStatus());
        assertEquals("API Key Expired. Regenerate new one.", res.getErrorMessage());
    }

 
    @Test
    void testRateLimitExceeded() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();

        req.addHeader("X-API-KEY", "key123");

        ApiKey key = new ApiKey();
        key.setActive(true);
        key.setExpiresAt(LocalDateTime.now().plusDays(1));

        when(apiKeyRepository.findByApiKey("key123")).thenReturn(Optional.of(key));
        when(rateLimiterService.allowRequest("key123")).thenReturn(false); // ❌ Rate limit exceeded

        boolean result = interceptor.preHandle(req, res, null);

        assertFalse(result);
        assertEquals(429, res.getStatus());
        assertEquals("Too Many Requests", res.getErrorMessage());
    }

 
    @Test
    void testValidApiKeySuccess() throws Exception {

        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();

        req.addHeader("X-API-KEY", "validKey");

        ApiKey key = new ApiKey();
        key.setActive(true);
        key.setExpiresAt(LocalDateTime.now().plusDays(3));

        when(apiKeyRepository.findByApiKey("validKey")).thenReturn(Optional.of(key));
        when(rateLimiterService.allowRequest("validKey")).thenReturn(true);

        boolean result = interceptor.preHandle(req, res, null);

        assertTrue(result);
        assertEquals(200, res.getStatus());  
    }
}
