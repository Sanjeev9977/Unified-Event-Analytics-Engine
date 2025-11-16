package com.example.analytics.controller;

import com.example.analytics.dto.EventSummaryDTO;
import com.example.analytics.dto.UserStatsDTO;
import com.example.analytics.entity.Event;
import com.example.analytics.interceptor.ApiKeyAuthInterceptor;
import com.example.analytics.repository.ApiKeyRepository;
import com.example.analytics.service.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;

import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(SpringExtension.class)
@WebMvcTest(AnalyticsController.class)
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

 
    @MockBean
    private EventService eventService;   

    @MockBean
    private ApiKeyRepository apiKeyRepository;  

    @MockBean
    private ApiKeyAuthInterceptor apiKeyAuthInterceptor;

    @BeforeEach
    void setup() throws Exception {
        doAnswer(invocation -> {
            HttpServletRequest request = invocation.getArgument(0);
            HttpServletResponse response = invocation.getArgument(1);
            if (request.getHeader("X-API-KEY") == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"X-API-KEY header is required\"}");
                return false;  
            }
            return true; 
        }).when(apiKeyAuthInterceptor).preHandle(any(), any(), any());
    }

 
    @Test
    void testCollectEvent_Success() throws Exception {
        Event event = new Event();
        event.setUserId("u1");
        event.setEventType("click");
        event.setTimestamp(LocalDateTime.now());

        Mockito.when(eventService.saveEvent(any(Event.class), eq("API123")))
                .thenReturn(event);

        mockMvc.perform(post("/api/analytics/collect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-KEY", "API123")
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isOk());
    }

   
    @Test
    void testEventSummary_MissingApiKey() throws Exception {
        mockMvc.perform(get("/api/analytics/event-summary"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("X-API-KEY header is required"));
    }

 
    @Test
    void testEventSummary_MissingEventParam() throws Exception {
        mockMvc.perform(get("/api/analytics/event-summary")
                        .header("X-API-KEY", "API123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("event parameter is required"));
    }
 
    @Test
    void testEventSummary_InvalidDateFormat() throws Exception {
        mockMvc.perform(get("/api/analytics/event-summary")
                        .header("X-API-KEY", "API123")
                        .param("event", "click")
                        .param("startDate", "2025-ABC"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid date format. Use yyyy-MM-dd"));
    }

 
    @Test
    void testEventSummary_StartAfterEnd() throws Exception {
        mockMvc.perform(get("/api/analytics/event-summary")
                        .header("X-API-KEY", "API123")
                        .param("event", "click")
                        .param("startDate", "2025-10-05")
                        .param("endDate", "2025-10-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("startDate cannot be after endDate"));
    }

 
    @Test
    void testGetEventSummary_Success() throws Exception {

    	String apiKey = "key123";
    	String event = "login";

    	LocalDateTime start = LocalDateTime.parse("2025-01-01T00:00:00.000000");
    	LocalDateTime end   = LocalDateTime.parse("2025-01-07T23:59:59.000000");

    	EventSummaryDTO summary = new EventSummaryDTO(
    	        "login",
    	        10L,
    	        5L,
    	        Map.of("mobile", 7L, "desktop", 3L),
    	        "2025-01-01",
    	        "2025-01-07"
    	);

    	when(eventService.getEventSummary(apiKey, event, start, end))
    	        .thenReturn(summary);
        mockMvc.perform(get("/api/analytics/event-summary")
                        .header("X-API-KEY", apiKey)
                        .param("event", event)
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-01-07"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.event").value("login"))
                .andExpect(jsonPath("$.count").value(10))
                .andExpect(jsonPath("$.uniqueUsers").value(5));
    }

 
    @Test
    void testGetUserStats_Success() throws Exception {
        UserStatsDTO dto = new UserStatsDTO();
        dto.setUserId("u1");
        dto.setTotalEvents(5);

        Mockito.when(eventService.getUserStats("API123", "u1"))
                .thenReturn(dto);

        mockMvc.perform(get("/api/analytics/user-stats")
                        .header("X-API-KEY", "API123")
                        .param("userId", "u1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("u1"))
                .andExpect(jsonPath("$.totalEvents").value(5));
    }
}
