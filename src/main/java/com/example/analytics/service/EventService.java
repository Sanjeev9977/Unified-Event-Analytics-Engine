package com.example.analytics.service;

import com.example.analytics.dto.EventSummaryDTO;
import com.example.analytics.dto.UserStatsDTO;
import com.example.analytics.entity.ApiKey;
import com.example.analytics.entity.Event;
import com.example.analytics.repository.ApiKeyRepository;
import com.example.analytics.repository.EventRepository;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EventService {

	private final EventRepository eventRepository;
    private final ApiKeyRepository apiKeyRepository;

    @Autowired
    public EventService(EventRepository eventRepository, ApiKeyRepository apiKeyRepository) {
        this.eventRepository = eventRepository;
        this.apiKeyRepository = apiKeyRepository;
    }
    @CacheEvict(value = "event-summary", allEntries = true)
    public Event saveEvent(Event event, String apiKey) {

        if (event.getUserId() == null || event.getUserId().isBlank()) {
            throw new RuntimeException("userId is required");
        }

        ApiKey app = apiKeyRepository.findByApiKey(apiKey)
                       .orElseThrow(() -> new RuntimeException("App not registered"));

        event.setApiKey(app);   

        return eventRepository.save(event);
    }

    @Cacheable(
    	    value = "event-summary",
    	    key = "#apiKey + ':' + #eventType + ':' + #start.toLocalDate() + ':' + #end.toLocalDate()"
    	)
    public EventSummaryDTO getEventSummary(String apiKey,String eventType, LocalDateTime start, LocalDateTime end) {
    	
    	 
        ApiKey app = apiKeyRepository.findByApiKey(apiKey)
                .orElseThrow(() -> new RuntimeException("App not registered"));

           
  
        
        List<Event> events = eventRepository.findByApiKey_ApiKey(apiKey);
        
        
        
        long count = events.stream()
                .filter(e -> e.getEventType() != null)
                .filter(e -> eventType.equals(e.getEventType()))
                .filter(e -> e.getTimestamp() != null &&
                             !e.getTimestamp().isBefore(start) &&
                             !e.getTimestamp().isAfter(end))
                .count();

         
        Map<String, Long> deviceData = new HashMap<>();

        events.stream()
                .filter(e -> e.getEventType() != null)
                .filter(e -> eventType.equals(e.getEventType()))
                .filter(e -> e.getTimestamp() != null &&
                             !e.getTimestamp().isBefore(start) &&
                             !e.getTimestamp().isAfter(end))
                .forEach(e -> deviceData.merge(
                        e.getDevice() == null ? "UNKNOWN" : e.getDevice(),
                        1L,
                        Long::sum
                ));
        
        Long uniqueUsers=events.stream().filter(e -> e.getEventType() != null).filter(e->eventType.equals(e.getEventType())).filter(e -> e.getTimestamp() != null && !e.getTimestamp().isBefore(start) && !e.getTimestamp().isAfter(end)).collect(Collectors.groupingBy(e->e.getUserId(),Collectors.counting())).entrySet().stream().count();

      

        return new EventSummaryDTO(
        	    eventType,
        	    count,
        	    uniqueUsers,
        	    deviceData,
        	    start.toString(),
        	    end.toString()
        	);
    }

    @Cacheable(
    	    value = "user-stats",
    	    key = "#apiKey + ':' + #userId"
    	)
    	public UserStatsDTO getUserStats(String apiKey, String userId) {
    	 
    	   ApiKey app = apiKeyRepository.findByApiKey(apiKey)
    	            .orElseThrow(() -> new RuntimeException("App not registered"));

    	    List<Event> events = eventRepository
    	            .findByUserIdAndApiKey_ApiKey(userId, app.getApiKey());

    	    if (events.isEmpty()) {
    	        return new UserStatsDTO();  
    	    }

    	    Event latest = events.get(events.size() - 1);

    	     
    	    Map<String, String> metadata = Optional.ofNullable(latest.getMetadata())
    	            .map(m -> {
    	                Hibernate.initialize(m);
    	                return new HashMap<>(m);  
    	            })
    	            .orElse(new HashMap<>());

    	     
    	    UserStatsDTO dto = new UserStatsDTO();
    	    dto.setUserId(userId);
    	    dto.setTotalEvents(events.size());
    	    dto.setDeviceDetails(metadata);
    	    dto.setIpAddress(latest.getIpAddress());

    	    return dto;   
    	}
    
    public void collectInternalEvent(Map<String, Object> map) {

        Event event = new Event();

      
        event.setEventType(String.valueOf(map.get("event")));
        event.setUrl(String.valueOf(map.get("url")));
        event.setUserId("short-url-user");   
        event.setDevice(String.valueOf(map.getOrDefault("device", "UNKNOWN")));
        event.setIpAddress(String.valueOf(map.getOrDefault("ipAddress", "0.0.0.0")));
        event.setTimestamp(LocalDateTime.now());

        
        Map<String, String> metadata = new HashMap<>();
        metadata.put("shortCode", String.valueOf(map.get("shortCode")));
        event.setMetadata(metadata);

        
        Object appIdObj = map.get("app_id");
        if (appIdObj == null) {
            throw new RuntimeException("Missing app_id in internal event");
        }

        Long appId;
        try {
            appId = Long.valueOf(appIdObj.toString());
        } catch (Exception ex) {
            throw new RuntimeException("Invalid app_id format: " + appIdObj);
        }

       
        Optional<ApiKey> opt = apiKeyRepository.findById(appId);

        if (opt.isEmpty()) {
            throw new RuntimeException("App not found for app_id " + appId);
        }

        
        String internalApiKey = opt.get().getApiKey();

         
        saveEvent(event, internalApiKey);
    }

    
    
 



}
