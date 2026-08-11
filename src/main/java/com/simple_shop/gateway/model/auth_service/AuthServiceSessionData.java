package com.simple_shop.gateway.model.auth_service;

import java.util.Objects;

public final class AuthServiceSessionData {
	private final Long userId;

	private AuthServiceSessionData(final AuthServiceSessionDataBuilder builder) throws IllegalStateException {
		super();

		Objects.requireNonNull(builder);

		builder.validate();

		this.userId = builder.getUserId();
	}

	public Long getUserId() {
		return this.userId;
	}

	public static final class AuthServiceSessionDataBuilder {
		private Long userId;

		public AuthServiceSessionDataBuilder() {
			super();
		}

		public Long getUserId() {
			return this.userId;
		}

		public AuthServiceSessionDataBuilder setUserId(final Long userId) {
			this.userId = userId;

			return this;
		}

		public void validate() throws IllegalStateException {
			if (this.userId != null && this.userId < 0) {
				throw new IllegalStateException("Invalid user ID!");
			}
		}

		public AuthServiceSessionData build() throws IllegalStateException {
			return new AuthServiceSessionData(this);
		}
	}
}