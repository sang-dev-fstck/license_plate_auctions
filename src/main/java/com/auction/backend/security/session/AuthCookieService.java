package com.auction.backend.security.session;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class AuthCookieService {
    @Value("${app.auth.cookie.secure:false}")
    private boolean secureCookie;

    @Value("${app.auth.cookie.same-site:Lax}")
    private String sameSite;

    public void addAuthCookie(HttpServletResponse response, String rawToken, Duration ttl) {
        ResponseCookie cookie = ResponseCookie.from(AuthCookieNames.ACCESS_TOKEN, rawToken)
                .httpOnly(true)
                .secure(secureCookie) // local dev dùng http nên false; production phải true
                .sameSite(sameSite)
                .path("/")
                .maxAge(ttl)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    public void clearAuthCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(AuthCookieNames.ACCESS_TOKEN, "")
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite(sameSite)
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

}
