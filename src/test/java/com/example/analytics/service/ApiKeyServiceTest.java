package com.example.analytics.service;

import com.example.analytics.entity.ApiKey;
import com.example.analytics.repository.ApiKeyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiKeyServiceTest {

    @Mock
    private ApiKeyRepository apiKeyRepository;

    @InjectMocks
    private ApiKeyService apiKeyService;

    private ApiKey apiKey;

    @BeforeEach
    void setup() {
        apiKey = new ApiKey();
        apiKey.setAppName("TestApp");
        apiKey.setApiKey("12345");
        apiKey.setActive(true);
        apiKey.setCreatedBy("user@test.com");
        apiKey.setCreatedAt(LocalDateTime.now());
        apiKey.setExpiresAt(LocalDateTime.now().plusDays(1));
    }

 
    @Test
    void testRegisterAppSuccess() {
        when(apiKeyRepository.findByAppName("MyApp"))
                .thenReturn(Optional.empty());

        when(apiKeyRepository.save(any(ApiKey.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ApiKey savedKey = apiKeyService.registerApp("MyApp", "admin");

        assertNotNull(savedKey);
        assertEquals("MyApp", savedKey.getAppName());
        assertEquals("admin", savedKey.getCreatedBy());
    }

    @Test
    void testRegisterAppFailure_AppAlreadyExists() {
        when(apiKeyRepository.findByAppName("MyApp"))
                .thenReturn(Optional.of(apiKey));

        assertThrows(RuntimeException.class,
                () -> apiKeyService.registerApp("MyApp", "admin"));
    }

 
    @Test
    void testCreateApiKeySuccess() {
        when(apiKeyRepository.findByAppName("App1"))
                .thenReturn(Optional.empty());

        when(apiKeyRepository.save(any(ApiKey.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ApiKey saved = apiKeyService.createApiKey("App1", "google@gmail.com");

        assertEquals("App1", saved.getAppName());
        assertEquals("google@gmail.com", saved.getCreatedBy());
        assertTrue(saved.isActive());
    }

    @Test
    void testCreateApiKeyFailure_AppExists() {
        when(apiKeyRepository.findByAppName("App1"))
                .thenReturn(Optional.of(apiKey));

        assertThrows(RuntimeException.class,
                () -> apiKeyService.createApiKey("App1", "google@gmail.com"));
    }

 
    @Test
    void testGetApiKey() {
        when(apiKeyRepository.findByAppName("TestApp"))
                .thenReturn(Optional.of(apiKey));

        Optional<ApiKey> result = apiKeyService.getApiKey("TestApp");

        assertTrue(result.isPresent());
        assertEquals("TestApp", result.get().getAppName());
    }
 
    @Test
    void testRevokeKeySuccess() {
        when(apiKeyRepository.findByAppName("TestApp"))
                .thenReturn(Optional.of(apiKey));
        when(apiKeyRepository.save(any(ApiKey.class))).thenReturn(apiKey);

        boolean revoked = apiKeyService.revokeKey("TestApp");

        assertTrue(revoked);
        assertFalse(apiKey.isActive());
    }

    @Test
    void testRevokeKey_AppNotFound() {
        when(apiKeyRepository.findByAppName("Unknown"))
                .thenReturn(Optional.empty());

        boolean result = apiKeyService.revokeKey("Unknown");

        assertFalse(result);
    }

 
    @Test
    void testRegenerateKeySuccess() {
        when(apiKeyRepository.findByAppName("TestApp"))
                .thenReturn(Optional.of(apiKey));

        when(apiKeyRepository.save(any(ApiKey.class)))
                .thenAnswer(i -> i.getArgument(0)); // return saved object

        ApiKey newKey = apiKeyService.regenerateKey("TestApp");

        assertNotNull(newKey.getApiKey());
        assertTrue(newKey.isActive());
        assertEquals("TestApp", newKey.getAppName());
    }

 
    @Test
    void testValidateKey_Success() {
        when(apiKeyRepository.findByApiKey("12345"))
                .thenReturn(Optional.of(apiKey));

        boolean result = apiKeyService.validateKey("12345");

        assertTrue(result);
    }

    @Test
    void testValidateKey_Expired() {
        apiKey.setExpiresAt(LocalDateTime.now().minusDays(1)); // expired

        when(apiKeyRepository.findByApiKey("12345"))
                .thenReturn(Optional.of(apiKey));

        boolean result = apiKeyService.validateKey("12345");

        assertFalse(result);
    }

    @Test
    void testValidateKey_NotFound() {
        when(apiKeyRepository.findByApiKey("wrong"))
                .thenReturn(Optional.empty());

        assertFalse(apiKeyService.validateKey("wrong"));
    }
}
