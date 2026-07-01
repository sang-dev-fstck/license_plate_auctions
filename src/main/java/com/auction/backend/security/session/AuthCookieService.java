package com.auction.backend.security.session;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class AuthCookieService {

    public void addAuthCookie(HttpServletResponse response, String rawToken, Duration ttl) {
        ResponseCookie cookie = ResponseCookie.from(AuthCookieNames.ACCESS_TOKEN, rawToken)
                .httpOnly(true)
                .secure(false) // local dev dùng http nên false; production phải true
                .sameSite("Lax")
                .path("/")
                .maxAge(ttl)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    public void clearAuthCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(AuthCookieNames.ACCESS_TOKEN, "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

}
