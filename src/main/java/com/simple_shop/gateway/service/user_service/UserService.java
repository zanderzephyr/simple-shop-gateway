package com.simple_shop.gateway.service.user_service;

import org.springframework.stereotype.Service;

import com.simple_shop.gateway.model.user_service.UserServiceUserType;
import com.simple_shop.gateway.utils.DataTypeUtils;

import reactor.core.publisher.Mono;

@Service
public class UserService {
	private final UserServiceBackendHttpClient client;

	public UserService(final UserServiceBackendHttpClient client) {
		super();

		this.client = client;
	}

	public Mono<UserServiceUserType> getUserType(final long userId) {
		return this.client.getUserType(userId)
				.map(userTypeDto -> DataTypeUtils.parseEnum(userTypeDto, UserServiceUserType.class));
	}
}