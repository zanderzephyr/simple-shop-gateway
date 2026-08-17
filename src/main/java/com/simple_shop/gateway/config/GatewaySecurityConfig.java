package com.simple_shop.gateway.config;

import java.util.Arrays;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.security.web.server.csrf.ServerCsrfTokenRequestAttributeHandler;
import org.springframework.security.web.server.csrf.ServerCsrfTokenRequestHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.WebFilter;

import reactor.core.publisher.Mono;

/*
 * The gateway uses non-blocking WebFlux. Spring Security in a reactive
 * environment is configured using a SecurityWebFilterChain (not
 * SecurityFilterChain). Its job is to handle CORS and protect the internal
 * management endpoints (session validation, user type validation, ...) to
 * ensure unauthorized traffic is blocked at the edge of the backend. The actual
 * services are synchronous WebMVC applications. They have no Spring Security
 * enabled so far, as the current approach relies on infrastructure security
 * instead. If this changes, the configuration should do: Keep CSRF disabled
 * since the gateway is the public endpoint. Enforce internal service-to-service
 * requests are allowed. Validate the custom incoming gateway headers exist.
 * Probably use some sort of encryption for the communication between the
 * services.
 *
 * We tackle CSRF attacks by setting a client-readable cookie named XSRF-TOKEN,
 * which clients are expected to copy and send back to us inside a custom HTTP
 * header named X-XSRF-TOKEN. While the browser automatically attaches the
 * XSRF-TOKEN cookie to all outgoing requests to our domain, only a legitimate
 * client application that can actually access our domain's storage can read its
 * text value and manually construct the required X-XSRF-TOKEN header line. This
 * completely prevents scenarios where a malicious site running in tab B tries
 * to execute a CSRF attack against an active session in tab A; tab B is
 * strictly blocked by the browser's Same-Origin Policy from reading the cookie
 * storage of our domain. TODO Better research this. OWASP offers a lot of
 * high-value information.
 */
@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {
	@Value("${simple-shop.allowed-origin}")
	private String allowedOrigin;

	public GatewaySecurityConfig() {
		super();
	}

	/*
	 * In the preflight request, these things are sent to the browser. The browser
	 * stores these values and applies them in the future communication. When max
	 * age passed, the browser sends another preflight message. The gateway only
	 * supplies these things to the browser - it usually doesn't use them itself.
	 */
	@Bean
	public CorsWebFilter corsWebFilter() {
		final CorsConfiguration config = new CorsConfiguration();

		/*
		 * Enables a good-willing browser to never pass responses to a requesting site,
		 * that is not specifically listed here.
		 */
		config.setAllowedOrigins(Collections.unmodifiableList(Arrays.asList(this.allowedOrigin)));

		/*
		 * Allows the browser to send the session cookie and whatever else the future
		 * holds in the section of user-related data. Credentials in this scenarios are
		 * not username and password.
		 */
		config.setAllowCredentials(Boolean.TRUE);

		config.setAllowedMethods(
				Collections.unmodifiableList(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")));

		/*
		 * Allowing "X-XSRF-TOKEN" for CSRF prevention.
		 */
		config.setAllowedHeaders(Collections.unmodifiableList(Arrays.asList("Content-Type", "X-XSRF-TOKEN")));

		config.setMaxAge(3600L);

		final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);

		return new CorsWebFilter(source);
	}

	@Bean
	public SecurityWebFilterChain securityWebFilterChain(final ServerHttpSecurity security) {
		return security //
				.csrf(csrf -> {
					final CookieServerCsrfTokenRepository repository = CookieServerCsrfTokenRepository
							.withHttpOnlyFalse();
					/*
					 * Angular expects the cookie to be named exactly XSRF-TOKEN and reads it
					 * automatically
					 */
					repository.setCookieName("XSRF-TOKEN");

					/*
					 * Validates every state-changing HTTP request (POST, PUT, DELETE, ...; usually
					 * no e.g. GET requests, as these are not expected to change the backend state
					 * by convention.) If the XSRF-TOKEN cookie or the X-XSRF-TOKEN fields are
					 * missing or do not match, the request is rejected. GET requests etc. are
					 * protected by our CORS config, not by CSRF defenses (The browser doesn't allow
					 * non-matching domains to touch CORS config protected contents).
					 */
					csrf.csrfTokenRepository(repository);

					final ServerCsrfTokenRequestHandler requestHandler = new ServerCsrfTokenRequestAttributeHandler();
					csrf.csrfTokenRequestHandler(requestHandler);
				})//
				.authorizeExchange(exchanges -> exchanges.anyExchange().permitAll())//
				.build();
	}

	/**
	 * Subscribes to the CSRF token attribute in every request, forcing Spring to
	 * commit the "XSRF-TOKEN" cookie back to the client on the first message
	 * without it.
	 */
	@Bean
	public WebFilter csrfCookieWebFilter() {
		return (exchange, chain) -> {
			final Mono<CsrfToken> csrfToken = exchange.getAttribute(CsrfToken.class.getName());
			if (csrfToken != null) {
				// Accessing the token forces Spring to write the cookie to the response stream
				return csrfToken.flatMap(_ -> chain.filter(exchange));
			}
			return chain.filter(exchange);
		};
	}
}
