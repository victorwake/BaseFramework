package com.example.base_framework.service;

import com.example.base_framework.dto.AuthResponse;
import com.example.base_framework.dto.ChangePasswordRequest;
import com.example.base_framework.dto.LoginRequest;
import com.example.base_framework.entity.User;
import com.example.base_framework.repository.UserRepository;
import com.example.base_framework.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthResponse login(LoginRequest request) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String accessToken = jwtService.generateToken(user.getEmail());
        String refreshToken = refreshTokenService.createRefreshToken(user).getToken();

        return new AuthResponse(accessToken, refreshToken);
    }

    public AuthResponse refresh(String refreshTokenValue) {

        var refreshToken = refreshTokenService.findByToken(refreshTokenValue);

        if (!refreshToken.isValid()) {
            throw new RuntimeException("Refresh token inválido o expirado");
        }

        User user = refreshToken.getUser();
        String newAccessToken = jwtService.generateToken(user.getEmail());
        String newRefreshToken = refreshTokenService.createRefreshToken(user).getToken();

        refreshTokenService.revokeToken(refreshTokenValue);

        return new AuthResponse(newAccessToken, newRefreshToken);
    }

    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("La contraseña actual no es correcta");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

}