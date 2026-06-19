package com.auction.backend.security.session;

public interface OpaqueTokenService {
    String generateToken();

    String hashToken(String rawToken);
}
