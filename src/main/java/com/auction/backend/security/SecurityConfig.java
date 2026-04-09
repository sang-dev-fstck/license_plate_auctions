package com.auction.backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    // Inject Anh thợ mỏ vào đây
    private final CustomUserDetailsService customUserDetailsService;

    // Thuật toán Băm (Hash) mật khẩu một chiều: BCrypt (Chuẩn an toàn hiện nay)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // BƯỚC NÂNG CẤP: Khai báo tường minh "Băng chuyền" kiểm tra mật khẩu
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        // 1. Chỉ định anh thợ mỏ
        authProvider.setUserDetailsService(customUserDetailsService);
        // 2. Chỉ định cỗ máy giải mã
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    // AuthenticationManager: Người điều phối việc đăng nhập
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // Cấu hình màng lọc an ninh chính
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
