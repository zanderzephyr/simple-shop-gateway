package com.simple_shop.gateway.dto.auth_service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.simple_shop.gateway.dto.formatter.JsonLongFormatter;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthServiceSessionDataDto {
	@JsonLongFormatter
	private Long userId;

	public AuthServiceSessionDataDto() {
		super();
	}

	public Long getUserId() {
		return this.userId;
	}

	public AuthServiceSessionDataDto setUserId(final Long userId) {
		this.userId = userId;

		return this;
	}
}