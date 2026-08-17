package com.simple_shop.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.simple_shop.gateway.model.auth_service.AuthServiceSessionVerification;
import com.simple_shop.gateway.model.user_service.UserServiceUserType;
import com.simple_shop.gateway.service.auth_service.AuthService;
import com.simple_shop.gateway.service.user_service.UserService;

import reactor.core.publisher.Mono;

@Component
public final class SessionVerificationFilter implements GlobalFilter, Ordered {
	private static final Logger logger = LoggerFactory.getLogger(SessionVerificationFilter.class);

	private static final String COOKIE_NAME = "session_id";

	private static final String USER_ID_HEADER = "X-User-Id";

	private static final String USER_TYPE_HEADER = "X-User-Type";

	private final AuthService authService;

	private final UserService userService;

	private final NoSessionRequestFilter noSessionRequestFilter = new NoSessionRequestFilter();

	private final UserTypeRequestFilter userTypeRequestFilter = new UserTypeRequestFilter();

	public SessionVerificationFilter(final AuthService authService, final UserService userService) {
		super();

		this.authService = authService;
		this.userService = userService;
	}

	@Override
	public Mono<Void> filter(final ServerWebExchange exchange, final GatewayFilterChain chain) {
		if (this.containsOurHeaders(exchange)) {
			return this.dropMaliciousRequest(exchange, chain);
		}
		final ServerHttpRequest request = exchange.getRequest();

		final HttpCookie sessionCookie = request.getCookies().getFirst(SessionVerificationFilter.COOKIE_NAME);
		if (sessionCookie == null || sessionCookie.getValue().isBlank()) {
			if (!this.noSessionRequestFilter.test(request)) {
				return this.handleUnauthorized(exchange);
			}
			return chain.filter(exchange);
		}

		final String sessionId = sessionCookie.getValue();
		return this.authService.verifySession(sessionId)
				.flatMap(verification -> this.processSessionVerification(verification, exchange, chain))
				.onErrorResume(t -> this.handleServerError(t, exchange));
	}

	private boolean containsOurHeaders(final ServerWebExchange exchange) {
		final HttpHeaders headers = exchange.getRequest().getHeaders();

		final boolean containsIdHeader = headers.containsHeader(SessionVerificationFilter.USER_ID_HEADER);
		final boolean containsTypeHeader = headers.containsHeader(SessionVerificationFilter.USER_TYPE_HEADER);

		return containsIdHeader || containsTypeHeader;
	}

	private Mono<Void> dropMaliciousRequest(final ServerWebExchange exchange, final GatewayFilterChain chain) {
		final ServerHttpRequest request = exchange.getRequest();

		final StringBuilder sb = new StringBuilder();

		sb.append("Malicious request detected!");
		sb.append(" ");
		sb.append("Path: ").append(this.sanitizeForLogging(request.getPath().value()));
		sb.append(" ");
		sb.append("HTTP method: ").append(request.getMethod().name());

		SessionVerificationFilter.logger.warn(sb.toString());

		return this.handleUnauthorized(exchange);
	}

	private String sanitizeForLogging(final String value) {
		return //
		value.replaceAll("\\r", "\\\\r")//
				.replaceAll("\\n", "\\\\n");
	}

	private Mono<Void> processSessionVerification(//
			final AuthServiceSessionVerification sessionVerification, //
			final ServerWebExchange exchange, //
			final GatewayFilterChain chain) {

		if (sessionVerification == null || sessionVerification.getSessionData() == null) {
			return this.handleUnauthorized(exchange);
		}

		final Long userId = sessionVerification.getSessionData().getUserId();
		/*
		 * Just protecting against broken messages. Actually not possible in the logical
		 * workflow.
		 */
		if (userId == null) {
			return this.handleUnauthorized(exchange);
		}

		return this.userService.getUserType(userId)
				.flatMap(userType -> this.processUserType(userId, userType, exchange, chain));
	}

	private Mono<Void> processUserType(//
			final Long userId, //
			final UserServiceUserType userType, //
			final ServerWebExchange exchange, //
			final GatewayFilterChain chain) {

		final ServerHttpRequest request = exchange.getRequest();
		if (userType == null || !this.userTypeRequestFilter.test(userType, request)) {
			return this.handleUnauthorized(exchange);
		}

		final ServerHttpRequest mutatedRequest = request.mutate()//
				.header(SessionVerificationFilter.USER_ID_HEADER, userId.toString())//
				.header(SessionVerificationFilter.USER_TYPE_HEADER, userType.name())//
				.build();

		return chain.filter(exchange.mutate().request(mutatedRequest).build());
	}

	/*
	 * Return HTTP 500, but do not allow anything else (like stack traces) to leak
	 * to the client.
	 */
	private Mono<Void> handleServerError(final Throwable t, final ServerWebExchange exchange) {
		final ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
		SessionVerificationFilter.logger.error("Authentication service failure", t);
		return response.setComplete();
	}

	private Mono<Void> handleUnauthorized(final ServerWebExchange exchange) {
		final ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(HttpStatus.UNAUTHORIZED);
		return response.setComplete();
	}

	@Override
	public int getOrder() {
		/*
		 * This reads strangely. Highest precedence is Integer.MIN_VALUE. Low values are
		 * executed first. Ties are possible, which would be resolved by Spring
		 * (possibly non-deterministicly). Later additions to the execution ordering of
		 * this project must create a robust and unambiguous environment.
		 */
		return 0;
	}
}
