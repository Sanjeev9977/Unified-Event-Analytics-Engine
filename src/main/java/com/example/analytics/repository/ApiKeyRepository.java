package com.example.analytics.repository;

 
 

import com.example.analytics.entity.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    Optional<ApiKey> findByAppName(String appName);
    Optional<ApiKey> findByApiKey(String apiKey);
}

