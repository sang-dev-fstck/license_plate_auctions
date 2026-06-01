package com.auction.backend.security.session;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OpaqueTokenAuthenticationFilter extends OncePerRequestFilter {

    private final OpaqueTokenService opaqueTokenService;
    private final AuthSessionRedisService authSessionRedisService;


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        try {
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }

            String rawToken = resolveCookieValue(request, AuthCookieNames.ACCESS_TOKEN);

            if (rawToken == null || rawToken.isBlank()) {
                filterChain.doFilter(request, response);
                return;
            }

            String tokenHash = opaqueTokenService.hashToken(rawToken);

            AuthSession authSession = authSessionRedisService.findByTokenHash(tokenHash)
                    .orElse(null);

            if (authSession == null) {
                filterChain.doFilter(request, response);
                return;
            }

            if (authSession.getExpiresAt() != null
                    && authSession.getExpiresAt().isBefore(LocalDateTime.now())) {
                authSessionRedisService.deleteByTokenHash(tokenHash);
                filterChain.doFilter(request, response);
                return;
            }

            List<SimpleGrantedAuthority> authorities = authSession.getRoles() == null
                    ? List.of()
                    : authSession.getRoles().stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            authSession.getEmail(),
                            null,
                            authorities
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            log.error("Failed to authenticate request by opaque token", e);
        }

        filterChain.doFilter(request, response);
    }

    private String resolveCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

}
