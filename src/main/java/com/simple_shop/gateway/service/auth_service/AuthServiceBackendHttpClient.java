package com.simple_shop.gateway.service.auth_service;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import com.simple_shop.gateway.dto.auth_service.AuthServiceSessionVerificationDto;

import reactor.core.publisher.Mono;

@HttpExchange("/api/auth/v1/backend")
interface AuthServiceBackendHttpClient {

	@GetExchange("/{sessionId}")
	Mono<AuthServiceSessionVerificationDto> verifySession(@PathVariable final String sessionId);
}
