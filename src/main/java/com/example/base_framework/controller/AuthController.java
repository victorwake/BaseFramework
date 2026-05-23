package com.example.base_framework.controller;

import com.example.base_framework.dto.AuthResponse;
import com.example.base_framework.dto.ChangePasswordRequest;
import com.example.base_framework.dto.LoginRequest;
import com.example.base_framework.dto.RefreshTokenRequest;
import com.example.base_framework.entity.User;
import com.example.base_framework.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Autenticación", description = "Endpoints de autenticación y gestión de sesión")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Iniciar sesión", description = "Autentica al usuario con email y contraseña, devuelve access token y refresh token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login exitoso"),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas"),
            @ApiResponse(responseCode = "429", description = "Demasiados intentos, espere un minuto")
    })
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @Operation(summary = "Renovar token", description = "Intercambia un refresh token válido por un nuevo par de tokens")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens renovados exitosamente"),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido o expirado")
    })
    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refresh(request.getRefreshToken());
    }

    @Operation(summary = "Perfil del usuario", description = "Devuelve los datos del usuario autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Datos del usuario"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping("/me")
    public User me(Authentication authentication) {
        return authService.getCurrentUser(authentication.getName());
    }

    @Operation(summary = "Cambiar contraseña", description = "Cambia la contraseña del usuario autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contraseña cambiada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Contraseña actual incorrecta o validación fallida"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        authService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok().build();
    }

}