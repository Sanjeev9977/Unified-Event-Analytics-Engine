package com.example.analytics.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.analytics.entity.ApiKey;
import com.example.analytics.entity.Event;
import com.example.analytics.repository.ApiKeyRepository;
import com.example.analytics.repository.EventRepository;

 

@Service
public class GlobleEventService {
	
	@Autowired
	 private  EventRepository eventRepository;
	    
	    @Autowired
	    private ApiKeyRepository apiKeyRepository;

	    

	    public Event saveEvent(Event event, String apiKey) {

	        if (event.getUserId() == null || event.getUserId().isBlank()) {
	            throw new RuntimeException("userId is required");
	        }

	        ApiKey app = apiKeyRepository.findByApiKey(apiKey)
	                       .orElseThrow(() -> new RuntimeException("App not registered"));

	        event.setApiKey(app);   

	        return eventRepository.save(event);
	    }


	    public Map<String, Object> getEventSummary(String eventType, LocalDateTime start, LocalDateTime end,Long appid) {
	    	
	    	
	        

	            
 
	        
	        List<Event> events = eventRepository.findByEventType(eventType);
	        
	        events.stream().forEach(e->{System.out.println(e.getApiKey().getId());});

	        
	        long count = events.stream()
	                .filter(e -> e.getTimestamp() != null)
	                .filter(e -> !e.getTimestamp().isBefore(start) &&
	                             !e.getTimestamp().isAfter(end))
	                .filter(e -> appid == null || 
	                             (e.getApiKey() != null && appid.equals(e.getApiKey().getId())))
	                .count();
             
 
	        Map<String, Long> deviceData = new HashMap<>();

	        events.stream()
	        .filter(e -> appid == null || (e.getApiKey() != null && appid.equals(e.getApiKey().getId())))
	                .filter(e -> e.getTimestamp() != null &&
	                             !e.getTimestamp().isBefore(start) &&
	                             !e.getTimestamp().isAfter(end))
	                .forEach(e -> deviceData.merge(
	                        e.getDevice() == null ? "UNKNOWN" : e.getDevice(),
	                        1L,
	                        Long::sum
	                ));
	        
	        Long uniqueUsers=events.stream().filter(e -> appid == null || (e.getApiKey() != null && appid.equals(e.getApiKey().getId()))).filter(e -> e.getTimestamp() != null && !e.getTimestamp().isBefore(start) && !e.getTimestamp().isAfter(end)).collect(Collectors.groupingBy(e->e.getUserId(),Collectors.counting())).entrySet().stream().count();

	        Map<String, Object> result = new HashMap<>();
	        result.put("event", eventType);
	        result.put("count", count);
	        result.put("uniqueUsers",uniqueUsers);
	        result.put("deviceData", deviceData);
	        result.put("startDate", start);
	        result.put("endDate", end);

	        return result;
	    }

	    public Map<String, Object> getUserStats(String userId) {

	        
	    	
	      
	        List<Event> events = eventRepository.findByUserId(userId);

	        if (events.isEmpty()) {
	            return Map.of("message", "No events found for user");
	        }

	       
	        Event latest = events.get(events.size() - 1);

	   
	        Map<String, String> deviceDetails =
	                latest.getMetadata() != null ? latest.getMetadata() : new HashMap<>();

	        Map<String, Object> result = new HashMap<>();
	        result.put("userId", userId);
	        result.put("totalEvents", events.size());
	        result.put("deviceDetails", deviceDetails);    
	        result.put("ipAddress", latest.getIpAddress());

	        return result;
	    }

	}

