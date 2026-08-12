package com.simple_shop.gateway.filter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;

class NoSessionRequestFilter extends CommonRequestFilter implements Predicate<ServerHttpRequest> {
	private static final Map<HttpMethod, Set<String>> WHITE_LIST = NoSessionRequestFilter.collectWhiteList();

	public NoSessionRequestFilter() {
		super();
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
	private static Map<HttpMethod, Set<String>> collectWhiteList() {
		final Map<HttpMethod, Set<String>> whiteList = new HashMap<>();

		final Set<String> getWhiteList = new HashSet<>();
		getWhiteList.add("/api/items/v1/regular/get-item/");
		getWhiteList.add("/api/items/v1/regular/get-items");
		whiteList.put(HttpMethod.GET, getWhiteList);

		final Set<String> postWhiteList = new HashSet<>();
		postWhiteList.add("/api/auth/v1/user/login");
		postWhiteList.add("/api/users/v1/regular/create-user");
		whiteList.put(HttpMethod.POST, postWhiteList);

		return whiteList;
	}

	@Override
	public boolean test(final ServerHttpRequest request) {
		return this.performTest(NoSessionRequestFilter.WHITE_LIST, request);
	}
}
