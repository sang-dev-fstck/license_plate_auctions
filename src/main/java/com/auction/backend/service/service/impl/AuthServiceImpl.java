package com.auction.backend.service.service.impl;

import com.auction.backend.dto.LoginRequest;
import com.auction.backend.dto.RegisterRequest;
import com.auction.backend.exception.AppException;
import com.auction.backend.repository.AccountRepository;
import com.auction.backend.repository.WalletRepository;
import com.auction.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AccountRepository accountRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    // Công cụ giúp Spring Boot 3.x chủ động lưu Session vào Redis
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    @Override
    public String register(RegisterRequest request) {
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new AppException("Email này đã được sử dụng");
        }

        return "";
    }

    @Override
    public String login(LoginRequest request) {
        return "";
    }
}
