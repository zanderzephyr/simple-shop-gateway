package com.simple_shop.gateway.filter;

import java.util.List;

abstract class CommonRequestFilter {
	public CommonRequestFilter() {
		super();
	}

	protected boolean performTest(final String path, final List<String> whiteList) {
		if (whiteList != null) {
			for (final String whiteListedPath : whiteList) {
				if (path.startsWith(whiteListedPath)) {
					return Boolean.TRUE;
				}
			}
		}
		return Boolean.FALSE;
	}
}
