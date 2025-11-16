package com.example.analytics.dto;

import java.time.LocalDateTime;

public class ApiKeyRequest {
    private String appName;
    private LocalDateTime expiresAt;

     
    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
