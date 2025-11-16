package com.example.analytics.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.analytics.entity.ApiKey;
import com.example.analytics.service.ApiKeyService;
import com.example.analytics.service.GoogleAuthService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final ApiKeyService apiKeyService;
    private final GoogleAuthService googleAuthService;

    public AuthController(ApiKeyService apiKeyService, GoogleAuthService googleAuthService) {
        this.apiKeyService = apiKeyService;
        this.googleAuthService = googleAuthService;
    }

   
    @PostMapping("/register")
    public Map<String, Object> register(@RequestParam String appName, @RequestParam String createdBy) {
        ApiKey apiKey = apiKeyService.registerApp(appName, createdBy);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "App registered successfully!");
        response.put("appName", apiKey.getAppName());
        response.put("apiKey", apiKey.getApiKey());
        response.put("expiresAt", apiKey.getExpiresAt());
        response.put("createdBy", apiKey.getCreatedBy());
        
        return response;
    }


    @PostMapping("/google-onboard")
    public ResponseEntity<?> onboardWithGoogle(@RequestParam String appName, @RequestBody Map<String, String> body) {
        try {
            String idToken = body.get("idToken");
            String email = googleAuthService.verifyGoogleToken(idToken);
            ApiKey apiKey = apiKeyService.createApiKey(appName, email);

            return ResponseEntity.ok(Map.of(
                    "message", "App onboarded successfully via Google",
                    "email", email,
                    "appName", apiKey.getAppName(),
                    "apiKey", apiKey.getApiKey(),
                    "expiresAt", apiKey.getExpiresAt()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }


 
    @GetMapping("/api-key")
    public ResponseEntity<Map<String, Object>> getApiKey(@RequestParam String appName) {
        return apiKeyService.getApiKey(appName)
                .map(apiKey -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("appName", apiKey.getAppName());
                    map.put("apiKey", apiKey.getApiKey());
                    map.put("expiresAt", apiKey.getExpiresAt());
                    map.put("active", apiKey.isActive());
                    return ResponseEntity.ok(map);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "App not found")));
    }

 
    @PostMapping("/revoke")
    public Map<String, Object> revokeKey(@RequestParam String appName) {
        boolean revoked = apiKeyService.revokeKey(appName);
        return Map.of("appName", appName,
                "revoked", revoked,
                "message", revoked ? "API Key revoked successfully" : "App not found");
    }
    
    @PostMapping("/regenerate")
    public ResponseEntity<?> regenerateKey(@RequestParam String appName) {

        Optional<ApiKey> optional = apiKeyService.getApiKey(appName);
        if (optional.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "App not found"));
        }

        ApiKey oldKey = optional.get();
        ApiKey newKey = apiKeyService.regenerateKey(appName);

        return ResponseEntity.ok(Map.of(
                "message", "API Key regenerated successfully",
                "oldKey", oldKey.getApiKey(),
                "newKey", newKey.getApiKey(),
                "expiresAt", newKey.getExpiresAt()
        ));
    }

    
}
