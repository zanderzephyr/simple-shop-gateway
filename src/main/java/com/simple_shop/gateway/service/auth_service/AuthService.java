package com.simple_shop.gateway.service.auth_service;

import org.springframework.stereotype.Service;

import com.simple_shop.gateway.model.auth_service.AuthServiceSessionVerification;

import reactor.core.publisher.Mono;

@Service
public class AuthService {
	private final AuthServiceBackendHttpClient client;

	public AuthService(final AuthServiceBackendHttpClient client) {
		super();

		this.client = client;
	}

	// TODO Probably deal with network issues or missing user services etc.
	public Mono<AuthServiceSessionVerification> verifySession(final String sessionId) {
		return this.client.verifySession(sessionId)//
				.map(AuthServiceDtoMapper::toDomain);
	}
}