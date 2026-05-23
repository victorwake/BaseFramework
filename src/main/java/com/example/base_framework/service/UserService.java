package com.example.base_framework.service;

import com.example.base_framework.dto.CreateUserRequest;
import com.example.base_framework.dto.UpdateUserRequest;
import com.example.base_framework.entity.Module;
import com.example.base_framework.entity.Role;
import com.example.base_framework.entity.User;
import com.example.base_framework.repository.ModuleRepository;
import com.example.base_framework.repository.RoleRepository;
import com.example.base_framework.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ModuleRepository moduleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<User> findAllUsersPaged(String search, Pageable pageable) {
        if (search == null || search.isBlank()) {
            return userRepository.findAll(pageable);
        }
        return userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(search, search, pageable);
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
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

    public User updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setName(request.getName());
        user.setEmail(request.getEmail());

        return userRepository.save(user);
    }

    public void updateUserRoles(Long userId, List<String> roleNames) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Role> roles = roleRepository.findByNameIn(roleNames);

        user.setRoles(new HashSet<>(roles));

        userRepository.save(user);
    }

    public void updateUserModules(Long userId, List<Long> moduleIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<Module> modules = moduleRepository.findAllById(moduleIds);

        user.setModules(new HashSet<>(modules));

        userRepository.save(user);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }

        userRepository.deleteById(id);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

}