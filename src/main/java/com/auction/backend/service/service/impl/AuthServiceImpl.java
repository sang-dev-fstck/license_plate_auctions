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
import com.auction.backend.security.session.*;
import com.auction.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private static final Duration AUTH_SESSION_TTL = Duration.ofHours(24);

    private final AccountRepository accountRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final OpaqueTokenService opaqueTokenService;
    private final AuthSessionRedisService authSessionRedisService;
    private final AuthCookieService authCookieService;

    @Override
    public String register(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());
        String phoneNumber = normalizePhone(request.getPhoneNumber());
        if (accountRepository.existsByEmail(email)) {
            throw AppException.conflict("email", "Email đã tồn tại");
        }

        if (accountRepository.existsByPhoneNumber(phoneNumber)) {
            throw AppException.conflict("phoneNumber", "Số điện thoại đã tồn tại");
        }
        Account account = Account.builder()
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .active(true)
                .phoneNumber(phoneNumber)
                .fullName(request.getFullName())
                .role(Role.BIDDER)
                .build();
        // 3. Persist Account - Lúc này ID sẽ được sinh ra
        Account savedAccount;
        savedAccount = accountRepository.save(account);
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

        Account account = accountRepository.findByEmail(normalizeEmail(request.getEmail()))
                .orElseThrow(() -> AppException.notFound("Không tìm thấy tài khoản hiện tại"));

        createOpaqueAuthSession(authentication, account, httpRequest, httpResponse);

        log.info("Login success for email={}", normalizeEmail(request.getEmail()));
        return "Đăng nhập thành công";
    }

    private void createOpaqueAuthSession(
            Authentication authentication,
            Account account,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        LocalDateTime now = LocalDateTime.now();

        String rawToken = opaqueTokenService.generateToken();
        String tokenHash = opaqueTokenService.hashToken(rawToken);

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        AuthSession authSession = AuthSession.builder()
                .tokenHash(tokenHash)
                .accountId(account.getId())
                .email(account.getEmail())
                .roles(roles)
                .createdAt(now)
                .expiresAt(now.plus(AUTH_SESSION_TTL))
                .lastSeenAt(now)
                .ipAddress(request.getRemoteAddr())
                .userAgent(request.getHeader("User-Agent"))
                .build();

        authSessionRedisService.save(authSession, AUTH_SESSION_TTL);
        authCookieService.addAuthCookie(response, rawToken, AUTH_SESSION_TTL);
    }

    @Override
    public CurrentUserResponse me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException("Người dùng chưa đăng nhập");
        }

        String email = authentication.getName();

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> AppException.notFound("Không tìm thấy tài khoản hiện tại"));

        return new CurrentUserResponse(
                account.getId(),
                account.getEmail(),
                account.getFullName(),
                account.getPhoneNumber(),
                account.getRole(),
                account.getActive()
        );
    }

    @Override
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        String rawToken = resolveCookieValue(request, AuthCookieNames.ACCESS_TOKEN);

        if (rawToken != null && !rawToken.isBlank()) {
            String tokenHash = opaqueTokenService.hashToken(rawToken);
            authSessionRedisService.deleteByTokenHash(tokenHash);
        }

        authCookieService.clearAuthCookie(response);

        SecurityContextHolder.clearContext();
        return "Đăng xuất thành công";
    }

    private String resolveCookieValue(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) {
            return null;
        }

        for (var cookie : request.getCookies()) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
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
