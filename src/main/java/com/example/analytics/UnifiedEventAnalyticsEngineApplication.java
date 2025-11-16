package com.example.analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class UnifiedEventAnalyticsEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(UnifiedEventAnalyticsEngineApplication.class, args);
	}

}
