package com.auction.backend.security.session;

import com.auction.backend.entity.Account;

import java.util.Optional;

public interface AuthSessionService {
    AuthSessionResult createSession(Account account, String userAgent, String ipAddress);

    Optional<AuthSession> findByRawToken(String rawToken);

    void revokeCurrent(String rawToken);

    void revokeAllByAccountId(String accountId);

}
