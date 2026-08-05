package com.simple_shop.gateway.utils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public final class GatewayUtils {
	private GatewayUtils() {
		throw new UnsupportedOperationException();
	}

	public static LocalDateTime createNow() {
		return ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime();
	}
}