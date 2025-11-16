package com.example.analytics.interceptor;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.example.analytics.entity.ApiKey;
import com.example.analytics.repository.ApiKeyRepository;
import com.example.analytics.service.RateLimiterService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ApiKeyAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private ApiKeyRepository apiKeyRepository;
    
    @Autowired
    private RateLimiterService rateLimiterService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String apiKey = request.getHeader("X-API-KEY");

        if (apiKey == null) {
            response.sendError(401, "Missing API Key");
            return false;
        }

        ApiKey key = apiKeyRepository.findByApiKey(apiKey).orElse(null);

        if (key == null || !key.isActive()) {
            response.sendError(401, "Invalid API Key");
            return false;
        }

        if (key.getExpiresAt().isBefore(LocalDateTime.now())) {
            response.sendError(401, "API Key Expired. Regenerate new one.");
            return false;
        }
        
   
        if (!rateLimiterService.allowRequest(apiKey)) {
        	response.sendError(429, "Too Many Requests");   
            return false;
        }

        return true;
    }
}
