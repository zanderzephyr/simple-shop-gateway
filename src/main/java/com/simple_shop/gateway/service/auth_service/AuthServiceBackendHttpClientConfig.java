package com.simple_shop.gateway.service.auth_service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.support.WebClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;

@Configuration
@ImportHttpServices(group = "auth-service-group", types = { AuthServiceBackendHttpClient.class })
public class AuthServiceBackendHttpClientConfig {
	private final String authServiceUrl;

	public AuthServiceBackendHttpClientConfig(@Value("${auth-service.url}") final String authServiceUrl) {
		super();

		this.authServiceUrl = authServiceUrl;
	}

	@Bean
	public WebClientHttpServiceGroupConfigurer authServiceGroupConfigurer() {
		return groups -> groups//
				.filterByName("auth-service-group") //
				.forEachClient((_, builder) -> builder//
						.baseUrl(this.authServiceUrl)//
						.defaultHeader("Accept", "application/json"));
	}
}
