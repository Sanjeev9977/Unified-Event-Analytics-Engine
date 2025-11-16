package com.example.analytics.controller;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.analytics.entity.Event;
import com.example.analytics.service.GlobleEventService;

@RestController
@RequestMapping("/api/globleAnalytics")
public class GlobleAnalyticsController {

    @Autowired
    private GlobleEventService globleEventService;

    
    @PostMapping("/collect")
    public Event collectEvent(@RequestBody Event event,
                              @RequestHeader("X-API-KEY") String apiKey) {
        return globleEventService.saveEvent(event, apiKey);
    }

  
    @GetMapping("/event-summary")
    public ResponseEntity<?> getEventSummary(
            @RequestParam(required = false) String event,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long appid) {

       
        if (event == null || event.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "event parameter is required"));
        }

        
        LocalDateTime start;
        LocalDateTime end;

        try {
            start = (startDate != null)
                    ? LocalDateTime.parse(startDate + "T00:00:00.000000")
                    : LocalDateTime.now().minusDays(7);

            end = (endDate != null)
                    ? LocalDateTime.parse(endDate + "T23:59:59.000000")
                    : LocalDateTime.now();

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid date format. Use yyyy-MM-dd"));
        }

        // Validate range
        if (start.isAfter(end)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "startDate cannot be after endDate"));
        }

        // Call service
        return ResponseEntity.ok(
                globleEventService.getEventSummary(event, start, end, appid)
        );
    }

   
    @GetMapping("/user-stats")
    public Map<String, Object> getUserStats(@RequestParam String userId) {
        return globleEventService.getUserStats(userId);
    }

}
