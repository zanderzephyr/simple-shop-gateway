package com.simple_shop.gateway.exceptions;

// TODO Move to common lib
public final class InvalidMessageException extends IllegalArgumentException {
	private static final long serialVersionUID = 3126015455269952391L;

	public InvalidMessageException() {
		super();
	}

	public InvalidMessageException(final String message) {
		super(message);
	}

	public InvalidMessageException(final Throwable throwable) {
		super(throwable);
	}

	public InvalidMessageException(final String message, final Throwable throwable) {
		super(message, throwable);
	}
}
