package com.example.base_framework.config;

import com.example.base_framework.entity.*;
import com.example.base_framework.entity.Module;
import com.example.base_framework.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final ModuleRepository moduleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {

        // MODULES
        Module dashboard = createModule("DASHBOARD");
        Module risk = createModule("RISK");

        // PERMISSIONS
        createPermission("USER_READ", dashboard);
        createPermission("USER_CREATE", dashboard);
        createPermission("USER_UPDATE", dashboard);
        createPermission("USER_DELETE", dashboard);

        createPermission("ROLE_READ", risk);
        createPermission("ROLE_CREATE", risk);
        createPermission("ROLE_UPDATE", risk);
        createPermission("ROLE_DELETE", risk);

        createPermission("PERMISSION_READ", risk);
        createPermission("PERMISSION_CREATE", risk);
        createPermission("PERMISSION_DELETE", risk);

        createPermission("MODULE_READ", risk);
        createPermission("MODULE_CREATE", risk);
        createPermission("MODULE_UPDATE", risk);
        createPermission("MODULE_DELETE", risk);

        // ROLES
        Role roleUser = createRole("ROLE_USER");
        Role roleAdmin = createRole("ROLE_ADMIN");
        Role roleSuperAdmin = createRole("ROLE_SUPER_ADMIN");

        assignAllPermissions(roleAdmin);
        assignAllPermissions(roleSuperAdmin);

        // ADMIN USER
        createDefaultAdmin(roleAdmin);
    }

    private Role createRole(String name) {
        return roleRepository.findByName(name).orElseGet(() ->
                roleRepository.save(Role.builder().name(name).build())
        );
    }

    private Module createModule(String name) {
        return moduleRepository.findByName(name).orElseGet(() ->
                moduleRepository.save(Module.builder().name(name).build())
        );
    }

    private void createPermission(String name, Module module) {
        if (!permissionRepository.existsByName(name)) {
            permissionRepository.save(
                    Permission.builder()
                            .name(name)
                            .module(module)
                            .build()
            );
        }
    }

    private void assignAllPermissions(Role role) {
        List<Permission> allPermissions = permissionRepository.findAll();
        role.setPermissions(new HashSet<>(allPermissions));
        roleRepository.save(role);
    }

    private void createDefaultAdmin(Role adminRole) {
        String email = "admin@email.com";

        if (userRepository.findByEmail(email).isPresent()) {
            log.info("Admin user already exists, skipping creation");
            return;
        }

        User admin = User.builder()
                .name("Admin")
                .email(email)
                .password(passwordEncoder.encode("admin123"))
                .roles(Set.of(adminRole))
                .build();

        userRepository.save(admin);
        log.info("Default admin user created: admin@email.com / admin123");
    }
}