package com.simple_shop.gateway.model.auth_service;

import java.util.Objects;

public final class AuthServiceSessionVerification {
	private final AuthServiceSessionData sessionData;

	private AuthServiceSessionVerification(final AuthServiceSessionVerificationBuilder builder) throws IllegalStateException {
		super();

		Objects.requireNonNull(builder);

		builder.validate();

		this.sessionData = builder.getSessionData();
	}

	public AuthServiceSessionData getSessionData() {
		return this.sessionData;
	}

	public static final class AuthServiceSessionVerificationBuilder {
		private AuthServiceSessionData sessionData;

		public AuthServiceSessionVerificationBuilder() {
			super();
		}

		public AuthServiceSessionData getSessionData() {
			return this.sessionData;
		}

		public AuthServiceSessionVerificationBuilder setSessionData(final AuthServiceSessionData sessionData) {
			this.sessionData = sessionData;

			return this;
		}

		public void validate() throws IllegalStateException {
			// Nothing so far.
		}

		public AuthServiceSessionVerification build() throws IllegalStateException {
			return new AuthServiceSessionVerification(this);
		}
	}
}