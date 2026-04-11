package com.auction.backend.service;

import com.auction.backend.dto.LoginRequest;
import com.auction.backend.dto.RegisterRequest;

public interface AuthService {
    String register(RegisterRequest request);

    String login(LoginRequest request);
}
