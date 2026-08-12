package com.simple_shop.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.simple_shop.gateway.service.auth_service.AuthService;
import com.simple_shop.gateway.service.user_service.UserService;

import reactor.core.publisher.Mono;

@Component
public final class SessionVerificationFilter implements GlobalFilter, Ordered {
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
		final ServerHttpRequest request = exchange.getRequest();

		final HttpCookie sessionCookie = request.getCookies().getFirst(SessionVerificationFilter.COOKIE_NAME);
		if (sessionCookie == null || sessionCookie.getValue().isBlank()) {
			if (!this.noSessionRequestFilter.test(request)) {
				return this.handleUnauthorized(exchange);
			}
			return chain.filter(exchange);
		}

		final String sessionId = sessionCookie.getValue();
		return this.authService.verifySession(sessionId).flatMap(sessionVerification -> {
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

			return this.userService.getUserType(userId).flatMap(userType -> {
				if (userType == null || !this.userTypeRequestFilter.test(userType, request)) {
					return this.handleUnauthorized(exchange);
				}

				final ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()//
						.header(SessionVerificationFilter.USER_ID_HEADER, userId.toString())//
						.header(SessionVerificationFilter.USER_TYPE_HEADER, userType.name())//
						.build();

				return chain.filter(exchange.mutate().request(mutatedRequest).build());
			});
		});
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
		 * (possibly non-deterministicly). This filter is positioned right after the
		 * highest priority elements Spring positions here. Later additions to the
		 * execution ordering of this project must create a robust and unambiguous
		 * environment.
		 */
		return Ordered.HIGHEST_PRECEDENCE + 1;
	}
}
