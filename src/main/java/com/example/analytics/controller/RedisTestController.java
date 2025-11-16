package com.example.analytics.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/redis")
public class RedisTestController {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    
    @Autowired
    private RedisConnectionFactory connectionFactory;

    @GetMapping("/set")
    public String set() {
        redisTemplate.opsForValue().set("test_key", "Hello Redis");
        return "Value Set!";
    }

    @GetMapping("/get")
    public Object get() {
        return redisTemplate.opsForValue().get("test_key");
    }
    
    @Cacheable("testCache")
    @GetMapping("/check")
    public String check() {
        System.out.println("Method executed...");
        return "OK";
    }
    
    @GetMapping("/ping")
    public String checkRedis() {
        try {
            String ping = connectionFactory.getConnection().ping();
            return "Redis PING Response: " + ping;
        } catch (Exception e) {
            return "Redis Connection FAILED: " + e.getMessage();
        }
    }

}
