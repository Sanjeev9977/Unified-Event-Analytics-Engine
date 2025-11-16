package com.example.analytics.controller;

import com.example.analytics.entity.ApiKey;
import com.example.analytics.entity.ShortUrl;
import com.example.analytics.interceptor.ApiKeyAuthInterceptor;
import com.example.analytics.service.EventService;
import com.example.analytics.service.ShortUrlService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShortUrlController.class)
public class ShortUrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShortUrlService service;

    @MockBean
    private EventService eventService;
    
    @MockBean
    private ApiKeyAuthInterceptor apiKeyAuthInterceptor;

    @BeforeEach
    void setup() throws Exception {
       
        when(apiKeyAuthInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

  
    @Test
    void testShorten() throws Exception {
        ShortUrl mockUrl = new ShortUrl();
        mockUrl.setShortCode("xyz123");
        mockUrl.setOriginalUrl("https://google.com");

        when(service.createShortUrl(anyString(), anyString()))
                .thenReturn(mockUrl);

        mockMvc.perform(post("/api/url/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-KEY", "abc123")
                        .content("{\"originalUrl\":\"https://google.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortUrl").value("http://localhost:8080/u/xyz123"))
                .andExpect(jsonPath("$.code").value("xyz123"));
    }

   
    @Test
    void testStats() throws Exception {
        ShortUrl mockUrl = new ShortUrl();
        mockUrl.setShortCode("xyz123");
        mockUrl.setOriginalUrl("https://google.com");
        mockUrl.setClickCount(15L);

        when(service.getByCode("xyz123")).thenReturn(mockUrl);

        mockMvc.perform(get("/api/url/stats/xyz123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalUrl").value("https://google.com"))
                .andExpect(jsonPath("$.shortUrl").value("http://localhost:8080/u/xyz123"))
                .andExpect(jsonPath("$.clicks").value(15));
    }

    @Test
    void testStats_NotFound() throws Exception {
        when(service.getByCode("invalid")).thenReturn(null);

        mockMvc.perform(get("/api/url/stats/invalid"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Short URL Not Found"));
    }
 
    @Test
    void testRedirect() throws Exception {
        ShortUrl mockUrl = new ShortUrl();
        mockUrl.setShortCode("abc111");
        mockUrl.setOriginalUrl("https://example.com");

        ApiKey key = new ApiKey();
        key.setId(10L);
        mockUrl.setApiKey(key);

        when(service.getByCode("abc111")).thenReturn(mockUrl);

        mockMvc.perform(get("/api/url/u/abc111")
                        .header("User-Agent", "JUnit")
                        .header("X-USER-ID", "user147")
                        .header("Referer", "https://ref.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("https://example.com"));

        verify(service, times(1)).updateAnalytics(mockUrl);

 
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(eventService).collectInternalEvent(captor.capture());

        Map<String, Object> event = captor.getValue();
        assert event.get("event").equals("short_url_click");
        assert event.get("shortCode").equals("abc111");
        assert event.get("userId").equals("user147");
        assert event.get("app_id").equals(10L);
    }

    @Test
    void testRedirect_NotFound() throws Exception {
        when(service.getByCode("404")).thenReturn(null);

        mockMvc.perform(get("/api/url/u/404"))
                .andExpect(status().isNotFound());
    }
}
