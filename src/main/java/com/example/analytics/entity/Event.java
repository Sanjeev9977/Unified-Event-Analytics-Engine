package com.example.analytics.entity;


 

import jakarta.persistence.*;
import java.time.LocalDateTime;

 

 
import java.util.Map;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String eventType;
    private String url;
    private String referrer;
    private String device;
    private String ipAddress;
    private String userId;
    
    @ManyToOne
    @JoinColumn(name = "app_id")
    private ApiKey apiKey;


    private LocalDateTime timestamp = LocalDateTime.now();

    @ElementCollection
    @CollectionTable(name = "event_metadata", joinColumns = @JoinColumn(name = "event_id"))
    @MapKeyColumn(name = "meta_key")
    @Column(name = "meta_value")
    private Map<String, String> metadata;
    
    public Event() {
    	
    }

	public Event(Long id, String eventType, String url, String referrer, String device, String ipAddress, String userId,
			ApiKey apiKey, LocalDateTime timestamp, Map<String, String> metadata) {
		 
		this.id = id;
		this.eventType = eventType;
		this.url = url;
		this.referrer = referrer;
		this.device = device;
		this.ipAddress = ipAddress;
		this.userId = userId;
		this.apiKey = apiKey;
		this.timestamp = timestamp;
		this.metadata = metadata;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getReferrer() {
		return referrer;
	}

	public void setReferrer(String referrer) {
		this.referrer = referrer;
	}

	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		this.device = device;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public ApiKey getApiKey() {
		return apiKey;
	}

	public void setApiKey(ApiKey apiKey) {
		this.apiKey = apiKey;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public Map<String, String> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, String> metadata) {
		this.metadata = metadata;
	}

 
	
	

     
    
}
