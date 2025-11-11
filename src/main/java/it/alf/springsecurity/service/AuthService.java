package it.alf.springsecurity.service;

import it.alf.springsecurity.dto.AuthResponse;
import it.alf.springsecurity.dto.LoginRequest;
import it.alf.springsecurity.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refresh(String refreshToken);
    void logout(String refreshToken);
}
