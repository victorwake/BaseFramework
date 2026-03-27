package com.example.base_framework.controller;

import com.example.base_framework.dto.CreateUserRequest;
import com.example.base_framework.entity.User;
import com.example.base_framework.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;


import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Obtener todos los usuarios")
    @PreAuthorize("hasAuthority('USER_READ')")
    @GetMapping
    public List<User> getUsers() {
        return userService.findAllUsers();
    }

    @PreAuthorize("hasAuthority('USER_CREATE')")
    @PostMapping
    public User createUser(@RequestBody CreateUserRequest request) {
        return userService.createUser(request);
    }

    @PreAuthorize("hasAuthority('USER_UPDATE')")
    @PutMapping("/{id}/roles")
    public void updateUserRoles(
            @PathVariable Long id,
            @RequestBody List<String> roles
    ) {
        userService.updateUserRoles(id, roles);
    }

    @PreAuthorize("hasAuthority('USER_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {

        userService.deleteUser(id);

        return ResponseEntity.noContent().build(); // 204
    }
}