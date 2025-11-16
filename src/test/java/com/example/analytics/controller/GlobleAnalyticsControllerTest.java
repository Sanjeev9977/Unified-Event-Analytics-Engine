package com.example.analytics.controller;

import com.example.analytics.entity.Event;
import com.example.analytics.service.GlobleEventService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;

import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class GlobleAnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GlobleEventService globleEventService;

    @Autowired
    private ObjectMapper objectMapper;

 
    @Test
    void testCollectEvent_Success() throws Exception {

        Event event = new Event();
        event.setUserId("u1");
        event.setEventType("login");
        event.setTimestamp(LocalDateTime.now());

        Mockito.when(globleEventService.saveEvent(any(Event.class), eq("API123")))
                .thenReturn(event);

        mockMvc.perform(post("/api/globleAnalytics/collect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-KEY", "API123")
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isOk());
    }

 
    @Test
    void testEventSummary_MissingEventParam() throws Exception {
        mockMvc.perform(get("/api/globleAnalytics/event-summary"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("event parameter is required"));
    }

 
    @Test
    void testEventSummary_InvalidDateFormat() throws Exception {
        mockMvc.perform(get("/api/globleAnalytics/event-summary")
                        .param("event", "login")
                        .param("startDate", "20-xyz"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid date format. Use yyyy-MM-dd"));
    }

  
    @Test
    void testEventSummary_StartAfterEnd() throws Exception {
        mockMvc.perform(get("/api/globleAnalytics/event-summary")
                        .param("event", "login")
                        .param("startDate", "2025-10-05")
                        .param("endDate", "2025-10-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("startDate cannot be after endDate"));
    }
 
    @Test
    void testGetEventSummary_Success() throws Exception {

        Map<String, Object> summary =
                Map.of(
                        "event", "login",
                        "count", 10,
                        "uniqueUsers", 5,
                        "deviceData", Map.of("mobile", 7, "desktop", 3),
                        "startDate", "2025-01-01",
                        "endDate", "2025-01-07"
                );

        Mockito.when(globleEventService.getEventSummary(
                        eq("login"),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class),
                        eq(1L)))
                .thenReturn(summary);

        mockMvc.perform(get("/api/globleAnalytics/event-summary")
                        .param("event", "login")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-01-07")
                        .param("appid", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.event").value("login"))
                .andExpect(jsonPath("$.count").value(10))
                .andExpect(jsonPath("$.uniqueUsers").value(5));
    }

 
    @Test
    void testUserStats_Success() throws Exception {

        Map<String, Object> userStats =
                Map.of(
                        "userId", "u1",
                        "totalEvents", 5,
                        "deviceDetails", Map.of("browser", "Chrome"),
                        "ipAddress", "127.0.0.1"
                );

        Mockito.when(globleEventService.getUserStats("u1"))
                .thenReturn(userStats);

        mockMvc.perform(get("/api/globleAnalytics/user-stats")
                        .param("userId", "u1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("u1"))
                .andExpect(jsonPath("$.totalEvents").value(5));
    }
}
