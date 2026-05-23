package com.example.base_framework.controller;

import com.example.base_framework.dto.AuthResponse;
import com.example.base_framework.dto.LoginRequest;
import com.example.base_framework.entity.Role;
import com.example.base_framework.entity.User;
import com.example.base_framework.repository.RoleRepository;
import com.example.base_framework.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        var role = roleRepository.save(Role.builder().name("ROLE_USER").build());
        var admin = roleRepository.save(Role.builder().name("ROLE_ADMIN").build());

        userRepository.save(User.builder()
                .name("Test User")
                .email("test@test.com")
                .password(passwordEncoder.encode("password123"))
                .roles(Set.of(role))
                .build());

        userRepository.save(User.builder()
                .name("Admin")
                .email("admin@test.com")
                .password(passwordEncoder.encode("admin123"))
                .roles(Set.of(admin))
                .build());
    }

    @Test
    void login_withValidCredentials_returnsTokens() {
        var request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("password123");

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/auth/login", request, AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isNotBlank();
        assertThat(response.getBody().getRefreshToken()).isNotBlank();
    }

    @Test
    void login_withInvalidPassword_returnsError() {
        var request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("wrongpassword");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/auth/login", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void login_withNonExistentUser_returnsError() {
        var request = new LoginRequest();
        request.setEmail("noexist@test.com");
        request.setPassword("password123");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/auth/login", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void me_withValidToken_returnsUser() {
        var loginReq = new LoginRequest();
        loginReq.setEmail("test@test.com");
        loginReq.setPassword("password123");

        var loginResponse = restTemplate.postForEntity("/auth/login", loginReq, AuthResponse.class);
        String token = loginResponse.getBody().getToken();

        var headers = new org.springframework.http.HttpHeaders();
        headers.setBearerAuth(token);

        var requestEntity = new org.springframework.http.HttpEntity<>(headers);
        ResponseEntity<User> response = restTemplate.exchange(
                "/auth/me",
                org.springframework.http.HttpMethod.GET,
                requestEntity,
                User.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getEmail()).isEqualTo("test@test.com");
    }

    @Test
    void me_withoutToken_returnsUnauthorized() {
        ResponseEntity<String> response = restTemplate.getForEntity("/auth/me", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void refresh_withValidToken_returnsNewTokens() {
        var loginReq = new LoginRequest();
        loginReq.setEmail("test@test.com");
        loginReq.setPassword("password123");

        var loginResponse = restTemplate.postForEntity("/auth/login", loginReq, AuthResponse.class);
        String refreshToken = loginResponse.getBody().getRefreshToken();

        var refreshReq = new com.example.base_framework.dto.RefreshTokenRequest();
        refreshReq.setRefreshToken(refreshToken);

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/auth/refresh", refreshReq, AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isNotBlank();
    }
}
