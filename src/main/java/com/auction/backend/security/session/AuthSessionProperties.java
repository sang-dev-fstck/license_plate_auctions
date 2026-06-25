package com.auction.backend.security.session;


import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "auction.auth.session")
@Getter
@Setter
@Validated
public class AuthSessionProperties {
    @Min(60)
    private long maxAgeSeconds = 86400;
}