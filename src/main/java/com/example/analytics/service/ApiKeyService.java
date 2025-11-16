package com.example.analytics.service;

import com.example.analytics.entity.ApiKey;
import com.example.analytics.repository.ApiKeyRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;

    public ApiKeyService(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    public ApiKey registerApp(String appName, String createdBy) {
        if (apiKeyRepository.findByAppName(appName).isPresent()) {
            throw new RuntimeException("App already registered with name: " + appName);
        }

        String key = ApiKey.generateKey();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(60);

        ApiKey apiKey = new ApiKey(appName, key, expiry, createdBy);  
        return apiKeyRepository.save(apiKey);
    }


   
    public ApiKey createApiKey(String appName, String email) {
        if (apiKeyRepository.findByAppName(appName).isPresent()) {
            throw new RuntimeException("App already registered with name: " + appName);
        }

        ApiKey apiKey = new ApiKey();
        apiKey.setAppName(appName);
        apiKey.setApiKey(UUID.randomUUID().toString().replace("-", ""));
        apiKey.setCreatedBy(email); // Google email
        apiKey.setActive(true);
        apiKey.setCreatedAt(LocalDateTime.now());
        apiKey.setExpiresAt(LocalDateTime.now().plusYears(1));

        return apiKeyRepository.save(apiKey);
    }

   
    public Optional<ApiKey> getApiKey(String appName) {
        return apiKeyRepository.findByAppName(appName);
    }

     
    public boolean revokeKey(String appName) {
        Optional<ApiKey> apiKeyOpt = apiKeyRepository.findByAppName(appName);
        if (apiKeyOpt.isPresent()) {
            ApiKey apiKey = apiKeyOpt.get();
            apiKey.setActive(false);
            apiKeyRepository.save(apiKey);
            return true;
        }
        return false;
    }
    
    public ApiKey regenerateKey(String appName) {
        ApiKey oldKey = apiKeyRepository.findByAppName(appName).orElseThrow();

        oldKey.setActive(false);
        apiKeyRepository.save(oldKey);

        ApiKey newKey = new ApiKey();
        newKey.setApiKey(UUID.randomUUID().toString());
        newKey.setAppName(appName);
        newKey.setCreatedBy(oldKey.getCreatedBy());
        newKey.setExpiresAt(LocalDateTime.now().plusMonths(6));
        newKey.setActive(true);

        return apiKeyRepository.save(newKey);
    }

    

    
    public boolean validateKey(String key) {
        Optional<ApiKey> apiKey = apiKeyRepository.findByApiKey(key);
        return apiKey.isPresent()
                && apiKey.get().isActive()
                && (apiKey.get().getExpiresAt() == null || apiKey.get().getExpiresAt().isAfter(LocalDateTime.now()));
    }
}
