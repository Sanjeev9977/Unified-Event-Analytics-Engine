package com.example.analytics.service;

import com.example.analytics.dto.EventSummaryDTO;
import com.example.analytics.dto.UserStatsDTO;
import com.example.analytics.entity.ApiKey;
import com.example.analytics.entity.Event;
import com.example.analytics.repository.ApiKeyRepository;
import com.example.analytics.repository.EventRepository;
import org.hibernate.Hibernate;
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
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ApiKeyRepository apiKeyRepository;

    @InjectMocks
    private EventService eventService;

    private ApiKey apiKey;
    private Event event;

    @BeforeEach
    void setup() {
        apiKey = new ApiKey();
        apiKey.setId(1L);
        apiKey.setApiKey("b4e6fb5d0e844085a9d3b8897d7dc48a7b0a7");
        apiKey.setAppName("TestApp");
        apiKey.setActive(true);
        apiKey.setCreatedAt(LocalDateTime.now());
        apiKey.setCreatedBy("user@gamil.com");
        apiKey.setExpiresAt(LocalDateTime.now().plusMinutes(20));

        event = new Event();
        event.setId(1L);
        event.setIpAddress("10.192.143");
        event.setReferrer("https://google.com");
        event.setUrl("https://google.com");
        event.setEventType("click");
        event.setUserId("user1");
        event.setDevice("mobile");
        event.setTimestamp(LocalDateTime.now());
        event.setApiKey(apiKey);
    }

 
    @Test
    void testSaveEventSuccess() {
        when(apiKeyRepository.findByApiKey("b4e6fb5d0e844085a9d3b8897d7dc48a7b0a7"))
                .thenReturn(Optional.of(apiKey));

        when(eventRepository.save(any(Event.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Event saved = eventService.saveEvent(event, "b4e6fb5d0e844085a9d3b8897d7dc48a7b0a7");

        assertNotNull(saved);
        assertEquals("user1", saved.getUserId());
        assertEquals(apiKey, saved.getApiKey());
    }

    @Test
    void testSaveEventMissingUserId() {
        event.setUserId("");

        assertThrows(RuntimeException.class,
                () -> eventService.saveEvent(event, "b4e6fb5d0e844085a9d3b8897d7dc48a7b0a7"));
    }

    @Test
    void testSaveEventAppNotRegistered() {
        when(apiKeyRepository.findByApiKey("b4e6fb5d0e844085a9d3b8897d7dc48a7b0a7"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> eventService.saveEvent(event, "b4e6fb5d0e844085a9d3b8897d7dc48a7b0a7"));
    }

 
    @Test
    void testGetEventSummarySuccess() {
        when(apiKeyRepository.findByApiKey("b4e6fb5d0e844085a9d3b8897d7dc48a7b0a7"))
                .thenReturn(Optional.of(apiKey));

        Event e1 = new Event();
        e1.setUserId("u1");
        e1.setEventType("click");
        e1.setTimestamp(LocalDateTime.now());
        e1.setDevice("mobile");

        Event e2 = new Event();
        e2.setUserId("u2");
        e2.setEventType("click");
        e2.setTimestamp(LocalDateTime.now());
        e2.setDevice("desktop");

        when(eventRepository.findByApiKey_ApiKey("b4e6fb5d0e844085a9d3b8897d7dc48a7b0a7"))
                .thenReturn(List.of(e1, e2));

        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end   = LocalDateTime.now().plusDays(1);

        EventSummaryDTO dto =
                eventService.getEventSummary("b4e6fb5d0e844085a9d3b8897d7dc48a7b0a7", "click", start, end);

        assertEquals("click", dto.getEvent());
        assertEquals(2L, dto.getCount());
        assertEquals(2L, dto.getUniqueUsers());
        assertEquals(2, dto.getDeviceData().size());
    }

    @Test
    void testGetEventSummary_AppNotRegistered() {
        when(apiKeyRepository.findByApiKey("b4e6fb5d0e844085a9d3b8897d7dc48a7b0a7"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> eventService.getEventSummary("b4e6fb5d0e844085a9d3b8897d7dc48a7b0a7", "click",
                        LocalDateTime.now(), LocalDateTime.now()));
    }

 
    @Test
    void testGetUserStatsSuccess() {
        when(apiKeyRepository.findByApiKey("b4e6fb5d0e844085a9d3b8897d7dc48a7b0a7"))
                .thenReturn(Optional.of(apiKey));

        Event e1 = new Event();
        e1.setUserId("user1");
        e1.setIpAddress("192.168.1.1");
        e1.setMetadata(Map.of("browser", "Chrome"));
        e1.setTimestamp(LocalDateTime.now());

        when(eventRepository.findByUserIdAndApiKey_ApiKey("user1", "b4e6fb5d0e844085a9d3b8897d7dc48a7b0a7"))
                .thenReturn(List.of(e1));

        UserStatsDTO dto = eventService.getUserStats("b4e6fb5d0e844085a9d3b8897d7dc48a7b0a7", "user1");

        assertEquals("user1", dto.getUserId());
        assertEquals(1, dto.getTotalEvents());
        assertEquals("192.168.1.1", dto.getIpAddress());
        assertEquals("Chrome", dto.getDeviceDetails().get("browser"));
    }

    @Test
    void testGetUserStats_NoEvents() {
        when(apiKeyRepository.findByApiKey(anyString()))
                .thenReturn(Optional.of(apiKey));

        when(eventRepository.findByUserIdAndApiKey_ApiKey(anyString(), anyString()))
                .thenReturn(Collections.emptyList());

        UserStatsDTO dto = eventService.getUserStats("API123", "user1");

        assertNotNull(dto);
        assertNull(dto.getUserId());
    }

    @Test
    void testGetUserStats_AppNotRegistered() {
        when(apiKeyRepository.findByApiKey("b4e6fb5d0e844085a9d3b8897d7dc48a7b0a7"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> eventService.getUserStats("b4e6fb5d0e844085a9d3b8897d7dc48a7b0a7", "user1"));
    }
 
    @Test
    void testCollectInternalEventSuccess() {

        Map<String, Object> map = new HashMap<>();
        map.put("event", "short-click");
        map.put("url", "https://test.com");
        map.put("device", "mobile");
        map.put("ipAddress", "1.1.1.1");
        map.put("shortCode", "xyz123");
        map.put("app_id", 1L);

        
        ApiKey appKey = new ApiKey();
        appKey.setId(1L);
        appKey.setApiKey("internal-key-111");

        when(apiKeyRepository.findById(1L))
                .thenReturn(Optional.of(appKey));

  
        when(apiKeyRepository.findByApiKey("internal-key-111"))
                .thenReturn(Optional.of(appKey));

        when(eventRepository.save(any(Event.class)))
                .thenAnswer(i -> i.getArgument(0));

        assertDoesNotThrow(() -> eventService.collectInternalEvent(map));
    }


}
