package com.simple_shop.gateway.service.user_service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.support.WebClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.HttpServiceGroup.ClientType;
import org.springframework.web.service.registry.ImportHttpServices;

@Configuration
@ImportHttpServices(//
		group = "user-service-group", //
		types = { UserServiceBackendHttpClient.class }, //
		clientType = ClientType.WEB_CLIENT)
public class UserServiceBackendHttpClientConfig {
	private final String userServiceUrl;

	public UserServiceBackendHttpClientConfig(@Value("${user-service.url}") final String userServiceUrl) {
		super();

		this.userServiceUrl = userServiceUrl;
	}

	@Bean
	public WebClientHttpServiceGroupConfigurer userServiceGroupConfigurer() {
		return groups -> groups//
				.filterByName("user-service-group") //
				.forEachClient((_, builder) -> builder//
						.baseUrl(this.userServiceUrl)//
						.defaultHeader("Accept", "application/json"));
	}
}
