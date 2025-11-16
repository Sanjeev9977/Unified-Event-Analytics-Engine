package com.example.analytics.controller;

 

import com.example.analytics.dto.UserStatsDTO;
import com.example.analytics.entity.Event;
 
import com.example.analytics.service.EventService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
    
	@Autowired
    private  EventService eventService;

 

     
    @PostMapping("/collect")
    public Event collectEvent(@RequestBody Event event, @RequestHeader("X-API-KEY") String apiKey) {
        // You can validate apiKey before saving (later add ApiKeyService)
        return eventService.saveEvent(event,apiKey);
    }

	 
    
    @GetMapping("/event-summary")
    public ResponseEntity<?> getEventSummary(
            @RequestHeader( "X-API-KEY") String apiKey,
            @RequestParam(required = false) String event,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        
        if (apiKey == null || apiKey.isBlank()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "X-API-KEY header is required"));
        }

         
        if (event == null || event.isBlank()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "event parameter is required"));
        }

        LocalDateTime start;
        LocalDateTime end;

        try {
            
            if (startDate != null) {
                start = LocalDateTime.parse(startDate + "T00:00:00.000000");
            } else {
                start = LocalDateTime.now().minusDays(7);
            }

            
            if (endDate != null) {
                end = LocalDateTime.parse(endDate + "T23:59:59.000000");
            } else {
                end = LocalDateTime.now();
            }

        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Invalid date format. Use yyyy-MM-dd"));
        }

        
        if (start.isAfter(end)) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "startDate cannot be after endDate"));
        }

        
        return ResponseEntity.ok(eventService.getEventSummary(apiKey, event, start, end));
    }

   
    @GetMapping("/user-stats")
    public  UserStatsDTO getUserStats(@RequestHeader("X-API-KEY") String apiKey,@RequestParam String userId) {
        return eventService.getUserStats(apiKey,userId);
    }
}
