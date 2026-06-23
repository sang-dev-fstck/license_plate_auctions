package com.auction.backend.security.session;

import com.auction.backend.entity.Account;
import com.auction.backend.enums.Role;
import com.auction.backend.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthSessionServiceImpl implements AuthSessionService {

    private final OpaqueTokenService opaqueTokenService;
    private final AuthSessionRedisService authSessionRedisService;
    private final AuthSessionProperties authSessionProperties;

    @Override
    public AuthSessionResult createSession(Account account, String userAgent, String ipAddress) {

        if (account == null
                || account.getId() == null
                || account.getEmail() == null
                || account.getRole() == null) {
            throw new IllegalArgumentException("Account is invalid for auth session creation");
        }

        long maxAgeSeconds = authSessionProperties.getMaxAgeSeconds();
        Duration ttl = Duration.ofSeconds(maxAgeSeconds);
        LocalDateTime now = LocalDateTime.now();

        String rawToken = opaqueTokenService.generateToken();
        String tokenHash = opaqueTokenService.hashToken(rawToken);

        List<String> roles = List.of(toAuthority(account.getRole()));

        AuthSession authSession = AuthSession.builder()
                .tokenHash(tokenHash)
                .accountId(account.getId())
                .email(account.getEmail())
                .roles(roles)
                .createdAt(now)
                .expiresAt(now.plusSeconds(maxAgeSeconds))
                .lastSeenAt(now)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        authSessionRedisService.save(authSession, ttl);
        authSessionRedisService.addToAccountIndex(account.getId(), tokenHash, ttl);
        return new AuthSessionResult(rawToken, maxAgeSeconds);
    }

    @Override
    public Optional<AuthSession> findByRawToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return Optional.empty();
        }
        String tokenHash = opaqueTokenService.hashToken(rawToken);
        Optional<AuthSession> authSessionOpt = authSessionRedisService.findByTokenHash(tokenHash);

        if (authSessionOpt.isEmpty()) {
            return Optional.empty();
        }

        AuthSession authSession = authSessionOpt.get();

        if (authSession.getExpiresAt() != null &&
                authSession.getExpiresAt().isBefore(LocalDateTime.now())) {
            authSessionRedisService.deleteByTokenHash(tokenHash);
            return Optional.empty();
        }
        return Optional.of(authSession);
    }

    @Override
    public void revokeCurrent(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }
        AuthSession session = findByRawToken(rawToken).orElseThrow(() -> new AppException("Auth session not found"));
        String tokenHash = opaqueTokenService.hashToken(rawToken);
        authSessionRedisService.removeFromAccountIndex(session.getAccountId(), tokenHash);
        authSessionRedisService.deleteByTokenHash(tokenHash);
    }

    @Override
    public void revokeAllByAccountId(String accountId) {
        if (accountId == null || accountId.isBlank()) {
            return;
        }
        Set<String> tokenHashes = authSessionRedisService.findTokenHashesByAccountId(accountId);
        for (String tokenHash : tokenHashes) {
            authSessionRedisService.deleteByTokenHash(tokenHash);
        }
        authSessionRedisService.deleteAccountIndex(accountId);
    }

    private String toAuthority(Role role) {
        String roleName = role.name();

        if (roleName.startsWith("ROLE_")) {
            return roleName;
        }

        return "ROLE_" + roleName;
    }

}
