package com.auction.backend.controller;

import com.auction.backend.dto.CurrentUserResponse;
import com.auction.backend.dto.LoginRequest;
import com.auction.backend.dto.RegisterRequest;
import com.auction.backend.dto.RevokeAllRequest;
import com.auction.backend.security.session.AuthSessionService;
import com.auction.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final AuthSessionService authSessionService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody @Valid LoginRequest request,
                                                     HttpServletRequest httpRequest,
                                                     HttpServletResponse httpResponse) {
        String stringResult = authService.login(request, httpRequest, httpResponse);
        return ResponseEntity.ok(Map.of(
                "message", stringResult
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> me() {
        return ResponseEntity.ok(authService.me());
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(authService.logout(request, response));
    }

    @GetMapping("/csrf")
    public ResponseEntity<Map<String, String>> csrf(CsrfToken csrfToken) {
        return ResponseEntity.ok(Map.of(
                "headerName", csrfToken.getHeaderName(),
                "parameterName", csrfToken.getParameterName(),
                "token", csrfToken.getToken()
        ));
    }

    @PostMapping("/revoke-all")
    public void revokeAll(@Valid @RequestBody RevokeAllRequest request) {
        authSessionService.revokeAllByAccountId(request.getAccountId());
    }
}
