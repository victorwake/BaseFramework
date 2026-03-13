package com.example.base_framework.service;

import com.example.base_framework.dto.CreateUserRequest;
import com.example.base_framework.entity.Role;
import com.example.base_framework.entity.User;
import com.example.base_framework.repository.RoleRepository;
import com.example.base_framework.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Set;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public User createUser(CreateUserRequest request) {

        Role userRole = roleRepository
                .findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Role not found"));

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(userRole))
                .build();

        return userRepository.save(user);
    }

}