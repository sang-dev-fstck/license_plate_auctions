package com.auction.backend.sercurity;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. Tắt CSRF (Bắt buộc cho REST API)
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Phân luồng giao thông
                .authorizeHttpRequests(auth -> auth
                        // Đường ưu tiên: Mở cửa tự do cho Đăng nhập, Đăng ký
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET,
                                "/api/plates/**",
                                "/api/categories/**",
                                "/api/tag-rules/**"
                        ).permitAll()
                        // QUY TẮC 3: MỞ CỬA CHO API SEARCH DÙNG METHOD POST
                        // Chỉ đích danh HTTP Method là POST và đúng URL search
                        .requestMatchers(org.springframework.http.HttpMethod.POST,
                                "/api/plates/search"
                        ).permitAll()
                        // Các đường còn lại (CRUD Biển số, Danh mục...): Bắt buộc phải có thẻ Session
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
