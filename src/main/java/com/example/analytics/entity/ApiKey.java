package com.example.analytics.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "api_keys")
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String appName;

    @Column(unique = true, nullable = false)
    private String apiKey;

    private boolean active = true;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime expiresAt;

 
    private String createdBy;
    
    @OneToMany(mappedBy = "apiKey", cascade = CascadeType.ALL)
    private List<Event> events;
    
    @OneToMany(mappedBy = "apiKey", cascade = CascadeType.ALL)
    private List<ShortUrl> shortUrls;

   
    public static String generateKey() {
        return UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().substring(0, 5);
    }

    public ApiKey() {}

    public ApiKey(String appName, String apiKey, LocalDateTime expiresAt, String createdBy) {
        this.appName = appName;
        this.apiKey = apiKey;
        this.expiresAt = expiresAt;
        this.createdBy = createdBy;
    }
    
    public ApiKey(String appName, String apiKey, LocalDateTime expiresAt) {
        this.appName = appName;
        this.apiKey = apiKey;
        this.expiresAt = expiresAt;
    }


    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAppName() { return appName; }
    public void setAppName(String appName) { this.appName = appName; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    
    
    
}
