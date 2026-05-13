package com.auction.backend.security;

import com.auction.backend.entity.Account;
import com.auction.backend.exception.AppException;
import com.auction.backend.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentAccountProvider {

    private final AccountRepository accountRepository;

    public Account getCurrentAccount() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException("Người dùng chưa đăng nhập");
        }

        String email = authentication.getName();

        return accountRepository.findByEmail(email)
                .orElseThrow(() -> new AppException("Không tìm thấy người dùng hiện tại"));
    }
}