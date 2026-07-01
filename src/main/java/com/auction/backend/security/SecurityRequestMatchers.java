package com.auction.backend.security;

import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.List;

public final class SecurityRequestMatchers {

    public static final RequestMatcher OPTIONS =
            new AntPathRequestMatcher("/**", "OPTIONS");
    public static final List<RequestMatcher> PUBLIC_AUTH_ENDPOINTS = List.of(
            new AntPathRequestMatcher("/api/v1/auth/register"),
            new AntPathRequestMatcher("/api/v1/auth/login"),
            new AntPathRequestMatcher("/api/v1/auth/csrf"),
            new AntPathRequestMatcher("/api/v1/health")
    );
    public static final List<RequestMatcher> PUBLIC_READ_ONLY_ENDPOINTS = List.of(
            new AntPathRequestMatcher("/api/v1/plates", "GET"),
            new AntPathRequestMatcher("/api/v1/categories", "GET"),
            new AntPathRequestMatcher("/api/v1/tag-rules", "GET"),
            new AntPathRequestMatcher("/api/v1/auction-sessions/customer", "GET"),
            new AntPathRequestMatcher("/api/v1/auction-sessions/**", "GET"),
            new AntPathRequestMatcher("/api/v1/plates/search", "POST"),
            new AntPathRequestMatcher("/api/v1/auction-sessions/customer", "POST")
    );
    public static final List<RequestMatcher> CSRF_IGNORED_ENDPOINTS = List.of(
            new AntPathRequestMatcher("/api/v1/plates/search", "POST"),
            new AntPathRequestMatcher("/api/v1/auction-sessions/customer", "POST")
    );
    public static final RequestMatcher SKIP_OPAQUE_TOKEN_FILTER =
            new OrRequestMatcher(concat(
                    List.of(OPTIONS),
                    PUBLIC_AUTH_ENDPOINTS,
                    PUBLIC_READ_ONLY_ENDPOINTS
            ));

    private SecurityRequestMatchers() {
    }

    public static RequestMatcher[] csrfIgnoredMatchers() {
        return CSRF_IGNORED_ENDPOINTS.toArray(RequestMatcher[]::new);
    }

    public static RequestMatcher[] publicMatchers() {
        return concat(
                PUBLIC_AUTH_ENDPOINTS,
                PUBLIC_READ_ONLY_ENDPOINTS
        ).toArray(RequestMatcher[]::new);
    }

    @SafeVarargs
    private static List<RequestMatcher> concat(List<RequestMatcher>... lists) {
        return java.util.Arrays.stream(lists)
                .flatMap(List::stream)
                .toList();
    }
}