package com.example.analytics.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class ShortUrl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String originalUrl;

    @Column(unique = true, nullable = false)
    private String shortCode;

    private Long clickCount = 0L;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime lastClicked;
    
    @ManyToOne
    @JoinColumn(name = "app_id")
    private ApiKey apiKey;
    
    
    
	public ApiKey getApiKey() {
		return apiKey;
	}

	public void setApiKey(ApiKey apiKey) {
		this.apiKey = apiKey;
	}

	public ShortUrl() {
	
	}

	 

	public ShortUrl(Long id, String originalUrl, String shortCode, Long clickCount, LocalDateTime createdAt,
			LocalDateTime lastClicked, ApiKey apiKey) {
		 
		this.id = id;
		this.originalUrl = originalUrl;
		this.shortCode = shortCode;
		this.clickCount = clickCount;
		this.createdAt = createdAt;
		this.lastClicked = lastClicked;
		this.apiKey = apiKey;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getOriginalUrl() {
		return originalUrl;
	}

	public void setOriginalUrl(String originalUrl) {
		this.originalUrl = originalUrl;
	}

	public String getShortCode() {
		return shortCode;
	}

	public void setShortCode(String shortCode) {
		this.shortCode = shortCode;
	}

	public Long getClickCount() {
		return clickCount;
	}

	public void setClickCount(Long clickCount) {
		this.clickCount = clickCount;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getLastClicked() {
		return lastClicked;
	}

	public void setLastClicked(LocalDateTime lastClicked) {
		this.lastClicked = lastClicked;
	}


    
    
    
}
