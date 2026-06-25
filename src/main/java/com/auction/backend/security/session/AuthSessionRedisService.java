package com.auction.backend.security.session;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

public interface AuthSessionRedisService {
    void save(AuthSession session, Duration ttl);

    Optional<AuthSession> findByTokenHash(String tokenHash);

    void deleteByTokenHash(String tokenHash);

    void addToAccountIndex(String accountId, String tokenHash, Duration ttl);

    Set<String> findTokenHashesByAccountId(String accountId);

    void removeFromAccountIndex(String accountId, String tokenHash);

    void deleteAccountIndex(String accountId);
}
