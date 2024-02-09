package com.flipkart.fc.Cache;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.flipkart.fc.Entity.User;

@Configuration
public class CacheBinConfig {

	@Bean
	public CacheStore<User> userCacheStore() {
		return new CacheStore<User>(Duration.ofMinutes(5)); 
	}
	
	@Bean
	public CacheStore<String> otpCacheStore() {
		return new CacheStore<String>(Duration.ofMinutes(1)); 
	}
	
}
