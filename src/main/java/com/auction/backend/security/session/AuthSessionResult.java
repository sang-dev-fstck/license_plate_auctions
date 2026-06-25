package com.auction.backend.security.session;

public record AuthSessionResult(
        String rawToken,
        long maxAgeSeconds
) {
}
