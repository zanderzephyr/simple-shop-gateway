package com.simple_shop.gateway.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiPredicate;

import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;

import com.simple_shop.gateway.model.user_service.UserServiceUserType;

/**
 * Determines session-based and user type based access to backend resources.
 * This filter has to be executed for resources hidden behind the session layer.
 */
class UserTypeRequestFilter extends CommonRequestFilter implements BiPredicate<UserServiceUserType, ServerHttpRequest> {
	private static final Map<HttpMethod, List<String>> CUSTOMER_WHITE_LIST = UserTypeRequestFilter
			.collectCustomerWhiteList();

	private static final Map<HttpMethod, List<String>> ADMIN_WHITE_LIST = UserTypeRequestFilter.collectAdminWhiteList();

	public UserTypeRequestFilter() {
		super();
	}

	private static Map<HttpMethod, List<String>> collectAdminWhiteList() {
		final Map<HttpMethod, List<String>> whiteList = new HashMap<>();

		for (final Entry<HttpMethod, List<String>> entry : UserTypeRequestFilter.CUSTOMER_WHITE_LIST.entrySet()) {
			whiteList.put(entry.getKey(), new ArrayList<>(entry.getValue()));
		}

		final List<String> deleteWhiteList = UserTypeRequestFilter.determineOrInit(HttpMethod.DELETE, whiteList);
		deleteWhiteList.add("/api/items/v1/regular/delete-item/");
		whiteList.put(HttpMethod.DELETE, deleteWhiteList);

		final List<String> getWhiteList = UserTypeRequestFilter.determineOrInit(HttpMethod.GET, whiteList);
		getWhiteList.add("/api/users/v1/regular/get-users");
		whiteList.put(HttpMethod.GET, getWhiteList);

		final List<String> postWhiteList = UserTypeRequestFilter.determineOrInit(HttpMethod.POST, whiteList);
		postWhiteList.add("/api/items/v1/regular/create-item");
		whiteList.put(HttpMethod.POST, postWhiteList);

		final List<String> putWhiteList = UserTypeRequestFilter.determineOrInit(HttpMethod.PUT, whiteList);
		putWhiteList.add("/api/items/v1/regular/modify-item/");
		whiteList.put(HttpMethod.PUT, putWhiteList);

		return whiteList;
	}

	private static List<String> determineOrInit(final HttpMethod httpMethod, final Map<HttpMethod, List<String>> map) {
		List<String> list = map.get(httpMethod);
		if (list == null) {
			list = new ArrayList<>();
			map.put(httpMethod, list);
		}
		return list;
	}

	private static Map<HttpMethod, List<String>> collectCustomerWhiteList() {
		final Map<HttpMethod, List<String>> whiteList = new HashMap<>();

		final List<String> deleteWhiteList = new ArrayList<>();
		deleteWhiteList.add("/api/users/v1/regular/delete-user/");
		whiteList.put(HttpMethod.DELETE, deleteWhiteList);

		final List<String> getWhiteList = new ArrayList<>();
		getWhiteList.add("/api/users/v1/regular/get-user/");
		getWhiteList.add("/api/users/v1/regular/get-user-type/");
		whiteList.put(HttpMethod.GET, getWhiteList);

		final List<String> postWhiteList = new ArrayList<>();
		postWhiteList.add("/api/auth/v1/user/logout");
		postWhiteList.add("/api/users/v1/regular/create-user");
		whiteList.put(HttpMethod.POST, postWhiteList);

		final List<String> putWhiteList = new ArrayList<>();
		putWhiteList.add("/api/users/v1/regular/modify-user/");
		whiteList.put(HttpMethod.PUT, putWhiteList);

		return whiteList;
	}

	@Override
	public boolean test(final UserServiceUserType userType, final ServerHttpRequest request) {
		final boolean result;

		final HttpMethod httpMethod = request.getMethod();
		final String path = request.getURI().getPath();
		if (UserServiceUserType.CUSTOMER.equals(userType)) {
			final List<String> whiteList = UserTypeRequestFilter.CUSTOMER_WHITE_LIST.get(httpMethod);
			result = this.performTest(path, whiteList);
		} else if (UserServiceUserType.ADMIN.equals(userType)) {
			final List<String> whiteList = UserTypeRequestFilter.ADMIN_WHITE_LIST.get(httpMethod);
			result = this.performTest(path, whiteList);
		} else {
			result = Boolean.FALSE;
		}

		return result;
	}
}
