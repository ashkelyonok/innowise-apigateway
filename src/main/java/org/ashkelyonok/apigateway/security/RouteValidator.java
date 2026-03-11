package org.ashkelyonok.apigateway.security;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.List;

@Component
public class RouteValidator {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private static final List<String> OPEN_API_ENDPOINTS = List.of(
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/actuator/health",
            "v3/api-docs",
            "swagger-ui",
            "webjars"
    );

    public boolean isSecured(ServerHttpRequest request) {
        String path = request.getURI().getPath();

        return OPEN_API_ENDPOINTS.stream()
                .noneMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }
}