package com.simple_shop.gateway.service.user_service;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import reactor.core.publisher.Mono;

@HttpExchange("/api/users/v1")
interface UserServiceBackendHttpClient {
	@GetExchange("/regular/get-user-type/{userId}")
	Mono<String> getUserType(@PathVariable Long userId);
}
