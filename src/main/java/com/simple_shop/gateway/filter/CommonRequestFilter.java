package com.simple_shop.gateway.filter;

import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;

abstract class CommonRequestFilter {
	public CommonRequestFilter() {
		super();
	}

	protected boolean performTest(final Map<HttpMethod, Set<String>> whiteList, final ServerHttpRequest request) {
		final String path = request.getURI().getPath();
		final Set<String> publicPaths = whiteList.get(request.getMethod());
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
}
