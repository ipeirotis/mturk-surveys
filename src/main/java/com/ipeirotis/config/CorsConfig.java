package com.ipeirotis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

	private static final String[] DEFAULT_ORIGINS = {
		"https://demographics.mturk-tracker.com",
		"https://mturk-demographics.appspot.com"
	};

	@Value("${cors.allowed-origins:}")
	private String extraOrigins;

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		String[] origins = buildOrigins();
		registry.addMapping("/api/**")
				.allowedOrigins(origins)
				.allowedMethods("GET", "OPTIONS")
				.allowedHeaders("*")
				.maxAge(3600);
	}

	private String[] buildOrigins() {
		if (extraOrigins == null || extraOrigins.isBlank()) {
			return DEFAULT_ORIGINS;
		}
		String[] extra = extraOrigins.split(",");
		String[] all = new String[DEFAULT_ORIGINS.length + extra.length];
		System.arraycopy(DEFAULT_ORIGINS, 0, all, 0, DEFAULT_ORIGINS.length);
		for (int i = 0; i < extra.length; i++) {
			all[DEFAULT_ORIGINS.length + i] = extra[i].trim();
		}
		return all;
	}
}
