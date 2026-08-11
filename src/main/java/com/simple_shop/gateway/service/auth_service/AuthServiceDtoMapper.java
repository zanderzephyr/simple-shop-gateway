package com.simple_shop.gateway.service.auth_service;

import com.simple_shop.gateway.dto.auth_service.AuthServiceSessionDataDto;
import com.simple_shop.gateway.dto.auth_service.AuthServiceSessionVerificationDto;
import com.simple_shop.gateway.exceptions.InvalidMessageException;
import com.simple_shop.gateway.model.auth_service.AuthServiceSessionData;
import com.simple_shop.gateway.model.auth_service.AuthServiceSessionData.AuthServiceSessionDataBuilder;
import com.simple_shop.gateway.model.auth_service.AuthServiceSessionVerification;
import com.simple_shop.gateway.model.auth_service.AuthServiceSessionVerification.AuthServiceSessionVerificationBuilder;

final class AuthServiceDtoMapper {
	private AuthServiceDtoMapper() {
		throw new UnsupportedOperationException();
	}

	public static AuthServiceSessionVerification toDomain(
			final AuthServiceSessionVerificationDto authServiceSessionVerificationDto) {

		try {
			final AuthServiceSessionDataDto sessionDataDto = authServiceSessionVerificationDto.getSessionData();
			return new AuthServiceSessionVerificationBuilder()//
					.setSessionData(sessionDataDto != null ? AuthServiceDtoMapper.toDomain(sessionDataDto) : null)//
					.build();
		} catch (final Exception e) {
			throw new InvalidMessageException(e);
		}
	}

	private static AuthServiceSessionData toDomain(final AuthServiceSessionDataDto authServiceSessionDataDto) {
		try {
			return new AuthServiceSessionDataBuilder()//
					.setUserId(authServiceSessionDataDto.getUserId())//
					.build();
		} catch (final Exception e) {
			throw new InvalidMessageException(e);
		}
	}
}
