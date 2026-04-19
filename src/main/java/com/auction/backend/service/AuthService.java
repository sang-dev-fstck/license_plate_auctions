package com.auction.backend.service;

import com.auction.backend.dto.CurrentUserResponse;
import com.auction.backend.dto.LoginRequest;
import com.auction.backend.dto.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    String register(RegisterRequest request);

    String login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse);

    CurrentUserResponse me();

    String logout(HttpServletRequest request, HttpServletResponse response);
}
