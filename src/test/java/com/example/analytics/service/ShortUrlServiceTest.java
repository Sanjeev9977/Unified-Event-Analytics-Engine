package com.example.analytics.service;

import com.example.analytics.entity.ApiKey;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.analytics.entity.ShortUrl;
import com.example.analytics.repository.ApiKeyRepository;
import com.example.analytics.repository.ShortUrlRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShortUrlServiceTest {

    private ShortUrlRepository shortUrlRepo;
    private ApiKeyRepository apiKeyRepo;
    private ShortUrlService service;

    @BeforeEach
    void setUp() {
        shortUrlRepo = mock(ShortUrlRepository.class);
        apiKeyRepo = mock(ApiKeyRepository.class);

        service = new ShortUrlService(shortUrlRepo);

         
        ReflectionTestUtils.setField(service, "apiKeyRepository", apiKeyRepo);
    }


    @Test
    void testCreateShortUrl_Success() {
        String apiKey = "key123";
        String url = "https://example.com";

        ApiKey app = new ApiKey();
        app.setApiKey(apiKey);

        when(apiKeyRepo.findByApiKey(apiKey)).thenReturn(Optional.of(app));
        when(shortUrlRepo.save(any(ShortUrl.class))).thenAnswer(i -> i.getArgument(0));

        ShortUrl result = service.createShortUrl(url, apiKey);

        assertNotNull(result);
        assertEquals(url, result.getOriginalUrl());
        assertEquals(app, result.getApiKey());
        assertNotNull(result.getShortCode());
        assertEquals(8, result.getShortCode().length());
    }

    @Test
    void testCreateShortUrl_WhenAppNotFound_ShouldThrow() {
        when(apiKeyRepo.findByApiKey("unknown")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                service.createShortUrl("https://abc.com", "unknown")
        );
    }

    @Test
    void testGetByCode_Success() {
        ShortUrl url = new ShortUrl();
        url.setShortCode("f424178f");

        when(shortUrlRepo.findByShortCode("f424178f")).thenReturn(Optional.of(url));

        ShortUrl result = service.getByCode("f424178f");

        assertEquals(url, result);
    }

    @Test
    void testGetByCode_InvalidCode_ShouldThrow() {
        when(shortUrlRepo.findByShortCode("invalid")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                service.getByCode("invalid")
        );
    }

    @Test
    void testUpdateAnalytics_ShouldIncreaseClickCountAndSave() {
        ShortUrl url = new ShortUrl();
        url.setClickCount(5L);


        service.updateAnalytics(url);

        assertEquals(6, url.getClickCount());
        assertNotNull(url.getLastClicked());

        verify(shortUrlRepo, times(1)).save(url);
    }

    @Test
    void testGetOriginalUrl_Success() {
        ShortUrl obj = new ShortUrl();
        obj.setOriginalUrl("https://google.com");

        when(shortUrlRepo.findByShortCode("f424178f")).thenReturn(Optional.of(obj));

        String result = service.getOriginalUrl("f424178f");

        assertEquals("https://google.com", result);
    }

    @Test
    void testGetOriginalUrl_NotFound_ShouldReturnNull() {
        when(shortUrlRepo.findByShortCode("xxx")).thenReturn(Optional.empty());

        assertNull(service.getOriginalUrl("xxx"));
    }
}
