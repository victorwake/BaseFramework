package com.example.base_framework.controller;

import com.example.base_framework.dto.AuthResponse;
import com.example.base_framework.dto.LoginRequest;
import com.example.base_framework.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

}