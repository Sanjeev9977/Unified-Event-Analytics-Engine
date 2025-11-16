package com.example.analytics.service;

 

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.stereotype.Service;
 

import java.util.Collections;

/*
 * @Service public class GoogleAuthService {
 * 
 * private static final String CLIENT_ID =
 * "716069722011-2unf0ola1j4tcbsaf9km9i2vacc4655o.apps.googleusercontent.com";
 * 
 * public String verifyGoogleToken(String idTokenString) throws Exception { var
 * transport = GoogleNetHttpTransport.newTrustedTransport(); var jsonFactory =
 * JacksonFactory.getDefaultInstance();
 * 
 * GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport,
 * jsonFactory) .setAudience(Collections.singletonList(CLIENT_ID)) .build();
 * 
 * GoogleIdToken idToken = verifier.verify(idTokenString); if (idToken != null)
 * { return idToken.getPayload().getEmail(); } else { throw new
 * RuntimeException("Failed to verify token: invalid ID token"); } } }
 */
@Service
public class GoogleAuthService {

    private final GoogleIdTokenVerifier verifier;

    public GoogleAuthService(GoogleIdTokenVerifier verifier) {
        this.verifier = verifier; 
    }

    public String verifyGoogleToken(String idTokenString) throws Exception {
        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken != null) {
            return idToken.getPayload().getEmail();
        } else {
            throw new RuntimeException("Failed to verify token: invalid ID token");
        }
    }
}

