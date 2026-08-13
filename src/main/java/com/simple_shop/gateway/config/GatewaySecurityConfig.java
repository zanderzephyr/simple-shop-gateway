package com.simple_shop.gateway.config;

import java.util.Arrays;
import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {
	public GatewaySecurityConfig() {
		super();
	}

	/*
	 * In the preflight request, these things are sent to the browser. The browser
	 * stores these values and applies them in the future communication. When max
	 * age passed, the browser sends another preflight message.
	 */
	@Bean
	public CorsWebFilter corsWebFilter() {
		final CorsConfiguration config = new CorsConfiguration();

		// TODO Use an entry in application.properties for that.
		config.setAllowedOrigins(Collections.unmodifiableList(Arrays.asList("http://localhost:4200")));

		/*
		 * Allows the browser to send the session cookie and whatever else the future
		 * holds in the section of user-related data. Credentials in this scenarios are
		 * not username and password.
		 */
		config.setAllowCredentials(Boolean.TRUE);

		config.setAllowedMethods(
				Collections.unmodifiableList(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")));

		config.setAllowedHeaders(Collections.unmodifiableList(Arrays.asList("Content-Type")));

		config.setMaxAge(3600L);

		final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);

		return new CorsWebFilter(source);
	}
}
