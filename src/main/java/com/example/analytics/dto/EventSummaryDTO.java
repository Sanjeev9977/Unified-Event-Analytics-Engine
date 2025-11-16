package com.example.analytics.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

 
public class EventSummaryDTO {
    private String event;
    private long count;
    private Long uniqueUsers;
    private Map<String, Long> deviceData;
    private String startDate;
    private String endDate;
    
    
    public EventSummaryDTO() {}
    
    
	public EventSummaryDTO(String event, long count, Long uniqueUsers, Map<String, Long> deviceData, String startDate,
			String endDate) {
		 
		this.event = event;
		this.count = count;
		this.uniqueUsers = uniqueUsers;
		this.deviceData = deviceData;
		this.startDate = startDate;
		this.endDate = endDate;
	}
	public String getEvent() {
		return event;
	}
	public void setEvent(String event) {
		this.event = event;
	}
	public long getCount() {
		return count;
	}
	public void setCount(long count) {
		this.count = count;
	}
	public Long getUniqueUsers() {
		return uniqueUsers;
	}
	public void setUniqueUsers(Long uniqueUsers) {
		this.uniqueUsers = uniqueUsers;
	}
	public Map<String, Long> getDeviceData() {
		return deviceData;
	}
	public void setDeviceData(Map<String, Long> deviceData) {
		this.deviceData = deviceData;
	}
	public String getStartDate() {
		return startDate;
	}
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	public String getEndDate() {
		return endDate;
	}
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
    
    
    
}