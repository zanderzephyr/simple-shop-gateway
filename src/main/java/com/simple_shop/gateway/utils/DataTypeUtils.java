package com.simple_shop.gateway.utils;

import java.util.Objects;

// TODO Move to library
public final class DataTypeUtils {
	private DataTypeUtils() {
		throw new UnsupportedOperationException();
	}

	public static <E extends Enum<E>> E parseEnum(final String name, final Class<E> enumClass)
			throws NullPointerException {
		Objects.requireNonNull(enumClass);

		if (name != null) {
			return Enum.valueOf(enumClass, name.trim());
		}
		return null;
	}
}