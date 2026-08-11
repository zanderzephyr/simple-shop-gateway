package com.simple_shop.gateway.dto.auth_service;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthServiceSessionVerificationDto {
	private AuthServiceSessionDataDto sessionData;

	public AuthServiceSessionVerificationDto() {
		super();
	}

	public AuthServiceSessionDataDto getSessionData() {
		return this.sessionData;
	}

	public AuthServiceSessionVerificationDto setSessionData(final AuthServiceSessionDataDto sessionData) {
		this.sessionData = sessionData;

		return this;
	}
}