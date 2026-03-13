package com.example.base_framework.service;

import com.example.base_framework.dto.AuthResponse;
import com.example.base_framework.dto.LoginRequest;
import com.example.base_framework.entity.User;
import com.example.base_framework.repository.UserRepository;
import com.example.base_framework.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse login(LoginRequest request) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        // por ahora devolvemos token dummy
        String token = jwtService.generateToken(user.getEmail());

        return new AuthResponse(token);
    }

}