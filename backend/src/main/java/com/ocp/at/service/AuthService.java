package com.ocp.at.service;

import com.ocp.at.dto.request.LoginRequest;
import com.ocp.at.dto.request.RegisterRequest;
import com.ocp.at.dto.request.TokenRefreshRequest;
import com.ocp.at.dto.response.JwtResponse;
import com.ocp.at.dto.response.TokenRefreshResponse;
import com.ocp.at.dto.response.UtilisateurResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    JwtResponse login(LoginRequest request, HttpServletRequest httpRequest);
    TokenRefreshResponse refreshToken(TokenRefreshRequest request);
    void logout(String refreshToken);
    UtilisateurResponse getCurrentUser(String email);
    UtilisateurResponse register(RegisterRequest request);
}
