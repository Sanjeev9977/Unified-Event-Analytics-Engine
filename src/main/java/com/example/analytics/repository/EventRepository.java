package com.example.analytics.repository;

import com.example.analytics.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e.eventType, COUNT(e) FROM Event e WHERE e.timestamp BETWEEN :start AND :end GROUP BY e.eventType")
    List<Object[]> aggregateByEventType(LocalDateTime start, LocalDateTime end);

    

    List<Event> findByApiKey_ApiKey(String apiKey);   

    List<Event> findByUserIdAndApiKey_ApiKey(String userId, String apiKey); 
    
    
    List<Event>    findByEventType(String eventType);
    
    List<Event>    findByUserId(String userId);
    
    
}

