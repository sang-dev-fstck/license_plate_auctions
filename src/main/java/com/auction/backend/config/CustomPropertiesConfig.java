package com.auction.backend.config;

import com.auction.backend.security.session.AuthSessionProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties(AuthSessionProperties.class)
@Configuration
public class CustomPropertiesConfig {
}
