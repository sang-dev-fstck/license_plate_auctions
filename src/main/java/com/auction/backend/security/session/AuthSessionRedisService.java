package com.auction.backend.security.session;

import java.time.Duration;
import java.util.Optional;

public interface AuthSessionRedisService {
    void save(AuthSession session, Duration ttl);

    Optional<AuthSession> findByTokenHash(String tokenHash);

    void deleteByTokenHash(String tokenHash);
}
