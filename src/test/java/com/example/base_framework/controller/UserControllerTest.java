package com.example.base_framework.controller;

import com.example.base_framework.dto.AuthResponse;
import com.example.base_framework.dto.CreateUserRequest;
import com.example.base_framework.dto.LoginRequest;
import com.example.base_framework.dto.UpdateUserRolesRequest;
import com.example.base_framework.entity.Permission;
import com.example.base_framework.entity.Role;
import com.example.base_framework.entity.User;
import com.example.base_framework.repository.PermissionRepository;
import com.example.base_framework.repository.RoleRepository;
import com.example.base_framework.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private String adminToken;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        permissionRepository.deleteAll();
        roleRepository.deleteAll();

        permissionRepository.save(Permission.builder().name("USER_READ").build());
        permissionRepository.save(Permission.builder().name("USER_CREATE").build());
        permissionRepository.save(Permission.builder().name("USER_UPDATE").build());
        permissionRepository.save(Permission.builder().name("USER_DELETE").build());

        var adminRole = Role.builder().name("ROLE_ADMIN").build();
        adminRole.setPermissions(Set.copyOf(permissionRepository.findAll()));
        roleRepository.save(adminRole);

        roleRepository.save(Role.builder().name("ROLE_USER").build());

        var admin = userRepository.save(User.builder()
                .name("Admin")
                .email("admin@test.com")
                .password(passwordEncoder.encode("admin123"))
                .roles(Set.of(adminRole))
                .build());

        var loginReq = new LoginRequest();
        loginReq.setEmail("admin@test.com");
        loginReq.setPassword("admin123");

        adminToken = restTemplate.postForEntity("/auth/login", loginReq, AuthResponse.class)
                .getBody().getToken();
    }

    private HttpEntity<?> withAuth(Object body) {
        var headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        return new HttpEntity<>(body, headers);
    }

    private HttpEntity<?> withAuthGet() {
        var headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        return new HttpEntity<>(headers);
    }

    @Test
    void getUsers_returnsList() {
        var response = restTemplate.exchange(
                "/api/users",
                HttpMethod.GET,
                withAuthGet(),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void createUser_returnsCreatedUser() {
        var request = new CreateUserRequest();
        request.setName("New User");
        request.setEmail("newuser@test.com");
        request.setPassword("password123");

        var response = restTemplate.exchange(
                "/api/users",
                HttpMethod.POST,
                withAuth(request),
                User.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getEmail()).isEqualTo("newuser@test.com");
    }

    @Test
    void getUserById_returnsUser() {
        var createReq = new CreateUserRequest();
        createReq.setName("Findable User");
        createReq.setEmail("findable@test.com");
        createReq.setPassword("password123");

        var created = restTemplate.exchange("/api/users", HttpMethod.POST, withAuth(createReq), User.class)
                .getBody();

        var response = restTemplate.exchange(
                "/api/users/" + created.getId(),
                HttpMethod.GET,
                withAuthGet(),
                User.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getEmail()).isEqualTo("findable@test.com");
    }

    @Test
    void updateUserRoles_updatesSuccessfully() {
        var createReq = new CreateUserRequest();
        createReq.setName("Role User");
        createReq.setEmail("roleuser@test.com");
        createReq.setPassword("password123");

        var created = restTemplate.exchange("/api/users", HttpMethod.POST, withAuth(createReq), User.class)
                .getBody();

        var rolesReq = new UpdateUserRolesRequest();
        rolesReq.setRoles(List.of("ROLE_ADMIN"));

        var response = restTemplate.exchange(
                "/api/users/" + created.getId() + "/roles",
                HttpMethod.PUT,
                withAuth(rolesReq),
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void deleteUser_removesUser() {
        var createReq = new CreateUserRequest();
        createReq.setName("Deletable User");
        createReq.setEmail("deletable@test.com");
        createReq.setPassword("password123");

        var created = restTemplate.exchange("/api/users", HttpMethod.POST, withAuth(createReq), User.class)
                .getBody();

        var response = restTemplate.exchange(
                "/api/users/" + created.getId(),
                HttpMethod.DELETE,
                withAuthGet(),
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
