package com.auction.backend.security;

import com.auction.backend.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
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
    private final ObjectMapper objectMapper; // inject từ Spring

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
                .csrf(AbstractHttpConfigurer::disable)
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/register", "/api/v1/auth/login").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET,
                                "/api/v1/plates",
                                "/api/v1/categories",
                                "/api/v1/tag-rules",
                                "/api/v1/auction-sessions/customer"
                        ).permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.POST,
                                "/api/v1/plates/search"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                {
                                    try {
                                        writeErrorResponse(
                                                response,
                                                HttpServletResponse.SC_UNAUTHORIZED,
                                                "Bạn chưa đăng nhập hoặc phiên đăng nhập đã hết hạn"
                                        );
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                        )
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                {
                                    try {
                                        writeErrorResponse(
                                                response,
                                                HttpServletResponse.SC_FORBIDDEN,
                                                "Bạn không có quyền truy cập tài nguyên này"
                                        );
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                        )
                );

        return http.build();
    }

    private void writeErrorResponse(HttpServletResponse response,
                                    int status,
                                    String message
    ) throws Exception {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        ErrorResponse errorResponse = new ErrorResponse(status, message);
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
