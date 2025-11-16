package com.example.analytics.service;

import com.example.analytics.entity.ApiKey;
import com.example.analytics.entity.Event;
import com.example.analytics.repository.ApiKeyRepository;
import com.example.analytics.repository.EventRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobleEventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ApiKeyRepository apiKeyRepository;

    @InjectMocks
    private GlobleEventService service;

    private ApiKey apiKey;
    private Event event;

    @BeforeEach
    void setup() {
        apiKey = new ApiKey();
        apiKey.setId(1L);
        apiKey.setApiKey("API123");
        apiKey.setAppName("TestApp");

        event = new Event();
        event.setUserId("user1");
        event.setEventType("click");
        event.setTimestamp(LocalDateTime.now());
        event.setDevice("mobile");
        event.setApiKey(apiKey);
    }
 
    @Test
    void testSaveEventSuccess() {

        when(apiKeyRepository.findByApiKey("API123"))
                .thenReturn(Optional.of(apiKey));

        when(eventRepository.save(any(Event.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Event saved = service.saveEvent(event, "API123");

        assertNotNull(saved);
        assertEquals("user1", saved.getUserId());
        assertEquals(apiKey, saved.getApiKey());
    }

    @Test
    void testSaveEventMissingUserId() {
        event.setUserId("");

        assertThrows(RuntimeException.class,
                () -> service.saveEvent(event, "API123"));
    }

    @Test
    void testSaveEventAppNotRegistered() {
        when(apiKeyRepository.findByApiKey("API123"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> service.saveEvent(event, "API123"));
    }

     
    @Test
    void testGetEventSummaryWithAppIdFilter() {

        Event e1 = new Event();
        e1.setUserId("u1");
        e1.setEventType("click");
        e1.setTimestamp(LocalDateTime.now());
        e1.setDevice("mobile");
        e1.setApiKey(apiKey);

        Event e2 = new Event();
        e2.setUserId("u2");
        e2.setEventType("click");
        e2.setTimestamp(LocalDateTime.now());
        e2.setDevice("desktop");
        e2.setApiKey(apiKey);

        when(eventRepository.findByEventType("click"))
                .thenReturn(List.of(e1, e2));

        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end   = LocalDateTime.now().plusDays(1);

        Map<String, Object> result =
                service.getEventSummary("click", start, end, 1L);

        assertEquals("click", result.get("event"));
        assertEquals(2L, result.get("count"));
        assertEquals(2L, result.get("uniqueUsers"));
        assertTrue(((Map) result.get("deviceData")).containsKey("mobile"));
        assertTrue(((Map) result.get("deviceData")).containsKey("desktop"));
    }

    @Test
    void testGetEventSummary_WithNoMatchingEvents() {

        when(eventRepository.findByEventType("click"))
                .thenReturn(Collections.emptyList());

        Map<String, Object> result =
                service.getEventSummary("click",
                        LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(1),
                        1L
                );

        assertEquals(0L, result.get("count"));
        assertEquals(0L, result.get("uniqueUsers"));
        assertTrue(((Map) result.get("deviceData")).isEmpty());
    }

    @Test
    void testGetEventSummary_NoAppIdProvided() {   

        Event e1 = new Event();
        e1.setUserId("u1");
        e1.setEventType("click");
        e1.setTimestamp(LocalDateTime.now());
        e1.setDevice("mobile");
        e1.setApiKey(apiKey);

        when(eventRepository.findByEventType("click"))
                .thenReturn(List.of(e1));

        Map<String, Object> result =
                service.getEventSummary("click",
                        LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(1),
                        null);

        assertEquals(1L, result.get("count"));
        assertEquals(1L, result.get("uniqueUsers"));
    }

    // =====================================================================
    // 3. getUserStats()
    // =====================================================================
    @Test
    void testGetUserStatsSuccess() {

        Event e1 = new Event();
        e1.setUserId("user1");
        e1.setIpAddress("192.168.1.1");
        e1.setMetadata(Map.of("browser", "Chrome"));

        when(eventRepository.findByUserId("user1"))
                .thenReturn(List.of(e1));

        Map<String, Object> result = service.getUserStats("user1");

        assertEquals("user1", result.get("userId"));
        assertEquals(1, result.get("totalEvents"));
        assertEquals("Chrome",
                ((Map) result.get("deviceDetails")).get("browser"));
        assertEquals("192.168.1.1", result.get("ipAddress"));
    }

    @Test
    void testGetUserStatsNoEvents() {

        when(eventRepository.findByUserId("user1"))
                .thenReturn(Collections.emptyList());

        Map<String, Object> result = service.getUserStats("user1");

        assertEquals("No events found for user", result.get("message"));
    }

}
