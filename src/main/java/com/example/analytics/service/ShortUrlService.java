package com.example.analytics.service;

import com.example.analytics.entity.ApiKey;

import com.example.analytics.entity.ShortUrl;
import com.example.analytics.repository.ApiKeyRepository;
import com.example.analytics.repository.ShortUrlRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ShortUrlService {

    private final ShortUrlRepository repo;

    public ShortUrlService(ShortUrlRepository repo) {
        this.repo = repo;
    }
    
    @Autowired
    private ApiKeyRepository apiKeyRepository;

    public ShortUrl createShortUrl(String url,String apiKey) {
       

        ApiKey app = apiKeyRepository.findByApiKey(apiKey)
                       .orElseThrow(() -> new RuntimeException("App not registered"));

         
    	
        ShortUrl obj = new ShortUrl();
        obj.setOriginalUrl(url);
        obj.setApiKey(app);
        obj.setShortCode(generateCode());
        return repo.save(obj);
    }

    public String generateCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    public ShortUrl getByCode(String code) {
        return repo.findByShortCode(code)
                .orElseThrow(() -> new RuntimeException("Invalid short code"));
    }

    public void updateAnalytics(ShortUrl url) {
        url.setClickCount(url.getClickCount() + 1);
        url.setLastClicked(LocalDateTime.now());
        repo.save(url);
    }
    
    public String getOriginalUrl(String code) {
        return repo.findByShortCode(code)
                .map(ShortUrl::getOriginalUrl)
                .orElse(null);
    }

}
