package com.example.analytics.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GoogleAuthServiceTest {

    @InjectMocks
    private GoogleAuthService googleAuthService;

    @Mock
    private GoogleIdTokenVerifier verifier;

    @Mock
    private GoogleIdToken googleIdToken;

    @Mock
    private Payload payload;

    @Test
    void testVerifyGoogleToken_Success() throws Exception {

         
        when(verifier.verify("valid-token")).thenReturn(googleIdToken);
        when(googleIdToken.getPayload()).thenReturn(payload);
        when(payload.getEmail()).thenReturn("test@example.com");

       
        String email = googleAuthService.verifyGoogleToken("valid-token");

        
        assertEquals("test@example.com", email);
    }

    @Test
    void testVerifyGoogleToken_InvalidToken() throws Exception {

        lenient().when(verifier.verify("invalid-token")).thenReturn(null);

        assertThrows(RuntimeException.class, () ->
                googleAuthService.verifyGoogleToken("invalid-token")
        );
    }

}
