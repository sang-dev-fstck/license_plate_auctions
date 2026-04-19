package com.auction.backend.service.service.impl;

import com.auction.backend.dto.LoginRequest;
import com.auction.backend.dto.RegisterRequest;
import com.auction.backend.entity.Account;
import com.auction.backend.entity.Wallet;
import com.auction.backend.enums.Role;
import com.auction.backend.exception.AppException;
import com.auction.backend.repository.AccountRepository;
import com.auction.backend.repository.WalletRepository;
import com.auction.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final AccountRepository accountRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public String register(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());
        String phoneNumer = normalizePhone(request.getPhoneNumber());
        if (accountRepository.existsByEmail(email)) {
            throw new AppException("This email already exists");
        }
        Account account = Account.builder()
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .active(true)
                .phoneNumber(phoneNumer)
                .fullName(request.getFullName())
                .role(Role.BIDDER)
                .build();
        // 3. Persist Account - Lúc này ID sẽ được sinh ra
        Account savedAccount;
        try {
            savedAccount = accountRepository.save(account);
        } catch (DataIntegrityViolationException e) {
            // Chặn đứng race condition nếu Index Unique bị vi phạm
            throw new AppException("Email hoặc số điện thoại đã tồn tại trong hệ thống");
        }
        try {
            Wallet wallet = Wallet.create(savedAccount.getId());
            walletRepository.save(wallet);
        } catch (Exception e) {
            accountRepository.deleteById(savedAccount.getId());
            throw new AppException("Không thể tạo ví");
        }
        log.info("Registered account {} with wallet", savedAccount.getEmail());
        return "Đăng ký tài khoản thành công";
    }

    @Override
    public String login(LoginRequest request) {
        return "";
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isEmpty()) {
            throw new AppException("Email cant not be empty");
        }
        return email.trim().toLowerCase();
    }

    private String normalizePhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            throw new AppException("Phone cant not be empty");
        }
        return phone.trim();
    }
}
