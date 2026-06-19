package com.auction.backend.security.session;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthSession {
    private String tokenHash;
    private String accountId;
    private String email;
    private List<String> roles;

    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime lastSeenAt;

    private String ipAddress;
    private String userAgent;
}
