package com.example.analytics.controller;

import com.example.analytics.entity.ShortUrl;
import com.example.analytics.service.EventService;
import com.example.analytics.service.ShortUrlService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/url")
public class ShortUrlController {

	@Autowired
    private  ShortUrlService service;
   
	@Autowired
    private  EventService eventService;
     
    @PostMapping("/shorten")
    public ResponseEntity<?> shorten(@RequestBody Map<String, String> req,@RequestHeader("X-API-KEY") String apiKey) {
        String url = req.get("originalUrl");
        ShortUrl obj = service.createShortUrl(url,apiKey);

        return ResponseEntity.ok(
                Map.of(
                        "shortUrl", "http://localhost:8080/u/" + obj.getShortCode(),
                        "code", obj.getShortCode()
                )
        );
    }

   
    @GetMapping("/stats/{code}")
    public ResponseEntity<?> stats(@PathVariable String code) {

        ShortUrl obj = service.getByCode(code);

        if (obj == null) {
            return ResponseEntity.status(404)
                    .body(Map.of("error", "Short URL Not Found"));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("originalUrl", obj.getOriginalUrl());
        response.put("shortUrl", "http://localhost:8080/u/" + code);
        response.put("clicks", obj.getClickCount());   

        return ResponseEntity.ok(response);
    }

   
	/*
	 * @GetMapping("/u/{code}") public void redirect(@PathVariable String code,
	 * HttpServletResponse res) throws IOException { ShortUrl obj =
	 * service.getByCode(code);
	 * 
	 * if (obj == null) { res.sendError(HttpServletResponse.SC_NOT_FOUND,
	 * "Invalid short code"); return; }
	 * 
	 * service.updateAnalytics(obj); res.sendRedirect(obj.getOriginalUrl()); }
	 */
    
    @GetMapping("/u/{code}")
    public void redirect(@PathVariable String code,
                         HttpServletResponse res,
                         HttpServletRequest request) throws IOException {

        ShortUrl obj = service.getByCode(code);

        if (obj == null) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid short code");
            return;
        }

 
        service.updateAnalytics(obj);

    
        Map<String, Object> analyticsEvent = new HashMap<>();
        analyticsEvent.put("event", "short_url_click");
        analyticsEvent.put("url", obj.getOriginalUrl());
        analyticsEvent.put("shortCode", obj.getShortCode());
        analyticsEvent.put("ipAddress", request.getRemoteAddr());
        analyticsEvent.put("device", request.getHeader("User-Agent"));
        analyticsEvent.put("timestamp", System.currentTimeMillis());
        analyticsEvent.put("referrer", request.getHeader("Referer"));

        String userId = Optional.ofNullable(request.getHeader("X-USER-ID"))
                .orElse("anonymous");
        analyticsEvent.put("userId", userId);
        
        analyticsEvent.put("app_id", obj.getApiKey().getId());

        
        eventService.collectInternalEvent(analyticsEvent);

     
        res.sendRedirect(obj.getOriginalUrl());
    }


}
