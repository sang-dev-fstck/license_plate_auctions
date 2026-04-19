package com.auction.backend.service.service.impl;

import com.auction.backend.dto.CurrentUserResponse;
import com.auction.backend.dto.LoginRequest;
import com.auction.backend.dto.RegisterRequest;
import com.auction.backend.entity.Account;
import com.auction.backend.entity.Wallet;
import com.auction.backend.enums.Role;
import com.auction.backend.exception.AppException;
import com.auction.backend.repository.AccountRepository;
import com.auction.backend.repository.WalletRepository;
import com.auction.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final AccountRepository accountRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();


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

    //Sau khi login thành công:
    //Spring tạo session
    //session id được gửi về cookie
    //nếu bạn dùng Spring Session + Redis, session data nằm trong Redis
    //request sau trình duyệt gửi kèm cookie
    //Spring load security context từ session
    //endpoint protected sẽ qua được
    @Override
    public String login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            normalizeEmail(request.getEmail()),
                            request.getPassword()
                    )
            );
        } catch (Exception e) {
            throw new AppException("Email hoặc mật khẩu không chính xác");
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        securityContextRepository.saveContext(context, httpRequest, httpResponse);
        log.info("Login success for email={}", normalizeEmail(request.getEmail()));
        return "Đăng nhập thành công";
    }

    @Override
    public CurrentUserResponse me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException("Người dùng chưa đăng nhập");
        }

        String email = authentication.getName();

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new AppException("Không tìm thấy tài khoản hiện tại"));

        return new CurrentUserResponse(
                account.getId(),
                account.getEmail(),
                account.getFullName(),
                account.getPhoneNumber(),
                account.getRole(),
                account.getActive()
        );
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
