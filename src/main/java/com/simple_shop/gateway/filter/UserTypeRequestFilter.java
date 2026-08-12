package com.simple_shop.gateway.filter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiPredicate;

import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;

import com.simple_shop.gateway.model.user_service.UserServiceUserType;

/**
 * Determines session-based and user type based access to backend resources.
 * This filter has to be executed for resources hidden behind the session layer.
 */
class UserTypeRequestFilter extends CommonRequestFilter implements BiPredicate<UserServiceUserType, ServerHttpRequest> {
	private static final Map<HttpMethod, Set<String>> ADMIN_WHITE_LIST = UserTypeRequestFilter.collectAdminWhiteList();

	private static final Map<HttpMethod, Set<String>> CUSTOMER_WHITE_LIST = UserTypeRequestFilter
			.collectCustomerWhiteList();

	public UserTypeRequestFilter() {
		super();
	}

	private static Map<HttpMethod, Set<String>> collectAdminWhiteList() {
		final Map<HttpMethod, Set<String>> whiteList = new HashMap<>();

		for (final Entry<HttpMethod, Set<String>> entry : UserTypeRequestFilter.collectCustomerWhiteList().entrySet()) {
			whiteList.put(entry.getKey(), new HashSet<>(entry.getValue()));
		}

		final Set<String> deleteWhiteList = UserTypeRequestFilter.determineOrInit(HttpMethod.DELETE, whiteList);
		deleteWhiteList.add("/api/items/v1/regular/delete-item/");
		whiteList.put(HttpMethod.DELETE, deleteWhiteList);

		final Set<String> getWhiteList = UserTypeRequestFilter.determineOrInit(HttpMethod.GET, whiteList);
		getWhiteList.add("/api/users/v1/regular/get-users");
		whiteList.put(HttpMethod.GET, getWhiteList);

		final Set<String> postWhiteList = UserTypeRequestFilter.determineOrInit(HttpMethod.POST, whiteList);
		postWhiteList.add("/api/items/v1/regular/create-item");
		whiteList.put(HttpMethod.POST, postWhiteList);

		final Set<String> putWhiteList = UserTypeRequestFilter.determineOrInit(HttpMethod.PUT, whiteList);
		putWhiteList.add("/api/items/v1/regular/modify-item/");
		whiteList.put(HttpMethod.PUT, putWhiteList);

		return whiteList;
	}

	private static Set<String> determineOrInit(final HttpMethod httpMethod, final Map<HttpMethod, Set<String>> map) {
		Set<String> set = map.get(httpMethod);
		if (set == null) {
			set = new HashSet<>();
			map.put(httpMethod, set);
		}
		return set;
	}

	private static Map<HttpMethod, Set<String>> collectCustomerWhiteList() {
		final Map<HttpMethod, Set<String>> whiteList = new HashMap<>();

		final Set<String> deleteWhiteList = new HashSet<>();
		deleteWhiteList.add("/api/users/v1/regular/delete-user/");
		whiteList.put(HttpMethod.DELETE, deleteWhiteList);

		final Set<String> getWhiteList = new HashSet<>();
		getWhiteList.add("/api/users/v1/regular/get-user/");
		getWhiteList.add("/api/users/v1/regular/get-user-type/");
		whiteList.put(HttpMethod.GET, getWhiteList);

		final Set<String> postWhiteList = new HashSet<>();
		postWhiteList.add("/api/auth/v1/user/logout");
		postWhiteList.add("/api/users/v1/regular/create-user");
		whiteList.put(HttpMethod.POST, postWhiteList);

		final Set<String> putWhiteList = new HashSet<>();
		putWhiteList.add("/api/users/v1/regular/modify-user/");
		whiteList.put(HttpMethod.PUT, putWhiteList);

		return whiteList;
	}

	@Override
	public boolean test(final UserServiceUserType userType, final ServerHttpRequest request) {
		final boolean result;

		if (UserServiceUserType.CUSTOMER.equals(userType)) {
			result = this.performTest(UserTypeRequestFilter.CUSTOMER_WHITE_LIST, request);
		} else if (UserServiceUserType.ADMIN.equals(userType)) {
			result = this.performTest(UserTypeRequestFilter.ADMIN_WHITE_LIST, request);
		} else {
			result = Boolean.FALSE;
		}

		return result;
	}
}
