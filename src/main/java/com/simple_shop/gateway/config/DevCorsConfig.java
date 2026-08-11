package com.simple_shop.gateway.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * Used in the development environment to allow the frontend on
 * localhost:frontendPort to work with the backend on localhost:gatewayPort.
 * Otherwise, the session cookies wouldn't be functional.
 */
@Configuration
public class DevCorsConfig {
	public DevCorsConfig() {
		super();
	}

	@Bean
	public CorsWebFilter corsWebFilter() {
		final CorsConfiguration config = new CorsConfiguration();

		// Default Angular dev server port
		config.setAllowedOrigins(List.of("http://localhost:4200"));
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
		config.setAllowedHeaders(List.of("Content-Type", "Authorization", "X-Requested-With"));
		config.setAllowCredentials(Boolean.TRUE);

		final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);

		return new CorsWebFilter(source);
	}
}
