package com.simple_shop.gateway.filter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.simple_shop.gateway.service.auth_service.AuthService;

import reactor.core.publisher.Mono;

@Component
public final class SessionVerificationFilter implements GlobalFilter, Ordered {
	private static final Map<HttpMethod, Set<String>> SESSION_WHITE_LIST = SessionVerificationFilter
			.collectSessionWhiteList();

	private static final String COOKIE_NAME = "session_id";

	private static final String USER_ID_HEADER = "X-User-Id";

	private final AuthService authService;

	public SessionVerificationFilter(final AuthService authService) {
		super();

		this.authService = authService;
	}

	/**
	 * This filter uses a white list approach, that has the basic assumption, no
	 * access-restricted endpoint is located under a public endpoint. All services
	 * have to respect that rule and move their access-restricted endpoints to a
	 * specific domain or make sure, the white-listing approach doesn't undermine
	 * their security.
	 *
	 * @return
	 */
	private static Map<HttpMethod, Set<String>> collectSessionWhiteList() {
		final Map<HttpMethod, Set<String>> whiteList = new HashMap<>();

		final Set<String> getWhiteList = new HashSet<>();
		getWhiteList.add("/api/items/v1/regular");
		whiteList.put(HttpMethod.GET, getWhiteList);

		final Set<String> postWhiteList = new HashSet<>();
		postWhiteList.add("/api/auth/v1/user/login");
		postWhiteList.add("/api/users/v1/regular");
		whiteList.put(HttpMethod.POST, postWhiteList);

		return whiteList;
	}

	@Override
	public Mono<Void> filter(final ServerWebExchange exchange, final GatewayFilterChain chain) {
		final ServerHttpRequest request = exchange.getRequest();

		final HttpCookie sessionCookie = request.getCookies().getFirst(SessionVerificationFilter.COOKIE_NAME);
		if (sessionCookie == null || sessionCookie.getValue().isBlank()) {
			if (this.isSessionWhiteListedRequest(request)) {
				return chain.filter(exchange);
			}
			return this.handleUnauthorized(exchange);
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

			final ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()//
					.header(SessionVerificationFilter.USER_ID_HEADER, userId.toString())//
					.build();

			return chain.filter(exchange.mutate().request(mutatedRequest).build());
		});
	}

	private boolean isSessionWhiteListedRequest(final ServerHttpRequest request) {
		final String path = request.getURI().getPath();
		final Set<String> publicPaths = SessionVerificationFilter.SESSION_WHITE_LIST.get(request.getMethod());
		if (publicPaths != null) {
			if (publicPaths.contains(path)) {
				return Boolean.TRUE;
			}

			for (final String publicPath : publicPaths) {
				if (path.startsWith(publicPath)) {
					return Boolean.TRUE;
				}
			}
		}
		return Boolean.FALSE;
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
