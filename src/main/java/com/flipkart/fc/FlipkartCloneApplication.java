package com.flipkart.fc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class FlipkartCloneApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlipkartCloneApplication.class, args);
	}

}
