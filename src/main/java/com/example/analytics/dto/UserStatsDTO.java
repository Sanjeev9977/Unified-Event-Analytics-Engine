package com.example.analytics.dto;

import java.util.Map;

public class UserStatsDTO {
    private String userId;
    private long totalEvents;
    private Map<String, String> deviceDetails;
    private String ipAddress;
	public UserStatsDTO(String userId, long totalEvents, Map<String, String> deviceDetails, String ipAddress) {
		 
		this.userId = userId;
		this.totalEvents = totalEvents;
		this.deviceDetails = deviceDetails;
		this.ipAddress = ipAddress;
	}
	public UserStatsDTO() {
		 
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public long getTotalEvents() {
		return totalEvents;
	}
	public void setTotalEvents(long totalEvents) {
		this.totalEvents = totalEvents;
	}
	public Map<String, String> getDeviceDetails() {
		return deviceDetails;
	}
	public void setDeviceDetails(Map<String, String> deviceDetails) {
		this.deviceDetails = deviceDetails;
	}
	public String getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
    
    
    
}
