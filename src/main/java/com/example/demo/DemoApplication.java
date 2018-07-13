package com.example.demo;

import com.example.config.LazyMvcEndpointHandlerMapping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@Import(LazyMvcEndpointHandlerMapping.class)
public class DemoApplication {
	
	@GetMapping("/")
	public String home() {
		return "Hello";
	}

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
}
