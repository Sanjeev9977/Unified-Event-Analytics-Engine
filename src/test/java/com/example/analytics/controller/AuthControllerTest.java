package com.example.analytics.controller;

import com.example.analytics.entity.ApiKey;
import com.example.analytics.service.ApiKeyService;
import com.example.analytics.service.GoogleAuthService;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApiKeyService apiKeyService;

    @MockBean
    private GoogleAuthService googleAuthService;

    @Autowired
    private ObjectMapper objectMapper;
 
    @Test
    void testRegister_Success() throws Exception {

        ApiKey apiKey = new ApiKey();
        apiKey.setAppName("myApp");
        apiKey.setApiKey("ABC123");
        apiKey.setCreatedBy("admin@test.com");
        apiKey.setExpiresAt(LocalDateTime.now());

        when(apiKeyService.registerApp("myApp", "admin@test.com")).thenReturn(apiKey);

        mockMvc.perform(post("/api/auth/register")
                        .param("appName", "myApp")
                        .param("createdBy", "admin@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appName").value("myApp"))
                .andExpect(jsonPath("$.apiKey").value("ABC123"));
    }

 
    @Test
    void testGoogleOnboard_Success() throws Exception {

        ApiKey apiKey = new ApiKey();
        apiKey.setAppName("myApp");
        apiKey.setApiKey("XYZ789");
        apiKey.setExpiresAt(LocalDateTime.now());

        when(googleAuthService.verifyGoogleToken("valid-token"))
                .thenReturn("user@gmail.com");

        when(apiKeyService.createApiKey("myApp", "user@gmail.com"))
                .thenReturn(apiKey);

        Map<String, String> body = Map.of("idToken", "valid-token");

        mockMvc.perform(post("/api/auth/google-onboard")
                        .param("appName", "myApp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@gmail.com"))
                .andExpect(jsonPath("$.apiKey").value("XYZ789"));
    }


    @Test
    void testGoogleOnboard_InvalidToken() throws Exception {

        when(googleAuthService.verifyGoogleToken(anyString()))
                .thenThrow(new RuntimeException("Invalid token"));

        Map<String, String> body = Map.of("idToken", "bad-token");

        mockMvc.perform(post("/api/auth/google-onboard")
                        .param("appName", "myApp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid token"));
    }
 
    @Test
    void testGetApiKey_Success() throws Exception {

        ApiKey apiKey = new ApiKey();
        apiKey.setAppName("myApp");
        apiKey.setApiKey("API123");
        apiKey.setActive(true);
        apiKey.setExpiresAt(LocalDateTime.now());

        when(apiKeyService.getApiKey("myApp")).thenReturn(Optional.of(apiKey));

        mockMvc.perform(get("/api/auth/api-key")
                        .param("appName", "myApp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appName").value("myApp"))
                .andExpect(jsonPath("$.apiKey").value("API123"));
    }

    @Test
    void testGetApiKey_NotFound() throws Exception {

        when(apiKeyService.getApiKey("unknown")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/auth/api-key")
                        .param("appName", "unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("App not found"));
    }

 
    @Test
    void testRevokeKey_Success() throws Exception {

        when(apiKeyService.revokeKey("myApp")).thenReturn(true);

        mockMvc.perform(post("/api/auth/revoke")
                        .param("appName", "myApp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revoked").value(true));
    }

    @Test
    void testRevokeKey_NotFound() throws Exception {

        when(apiKeyService.revokeKey("unknown")).thenReturn(false);

        mockMvc.perform(post("/api/auth/revoke")
                        .param("appName", "unknown"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revoked").value(false));
    }
 
    @Test
    void testRegenerateKey_Success() throws Exception {

        ApiKey oldKey = new ApiKey();
        oldKey.setApiKey("OLD123");

        ApiKey newKey = new ApiKey();
        newKey.setApiKey("NEW456");
        newKey.setExpiresAt(LocalDateTime.now());

        when(apiKeyService.getApiKey("myApp")).thenReturn(Optional.of(oldKey));
        when(apiKeyService.regenerateKey("myApp")).thenReturn(newKey);

        mockMvc.perform(post("/api/auth/regenerate")
                        .param("appName", "myApp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.oldKey").value("OLD123"))
                .andExpect(jsonPath("$.newKey").value("NEW456"));
    }

    @Test
    void testRegenerateKey_NotFound() throws Exception {

        when(apiKeyService.getApiKey("unknown")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/regenerate")
                        .param("appName", "unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("App not found"));
    }
}
