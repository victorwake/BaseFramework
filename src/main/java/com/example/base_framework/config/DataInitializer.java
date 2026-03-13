package com.example.base_framework.config;

import com.example.base_framework.entity.*;
import com.example.base_framework.entity.Module;
import com.example.base_framework.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final ModuleRepository moduleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public void run(String... args) {

        // ROLES
        createRole("ROLE_ADMIN");
        createRole("ROLE_USER");
        createRole("ROLE_SUPER_ADMIN");

        // MODULES
        createModule("DASHBOARD");
        createModule("RISK");

        // PERMISSIONS
        createPermission("USER_READ");
        createPermission("USER_CREATE");
        createPermission("USER_UPDATE");
        createPermission("USER_DELETE");

        assignPermissionsToAdmin();


    }
    private void createRole(String name) {

        if (!roleRepository.existsByName(name)) {
            roleRepository.save(
                    Role.builder()
                            .name(name)
                            .build()
            );
        }

    }

    private void createModule(String name) {

        if (!moduleRepository.existsByName(name)) {
            moduleRepository.save(
                    Module.builder()
                            .name(name)
                            .build()
            );
        }

    }

    private void createPermission(String name) {

        if (!permissionRepository.existsByName(name)) {
            permissionRepository.save(
                    Permission.builder()
                            .name(name)
                            .build()
            );
        }
    }

    private void assignPermissionsToAdmin() {

        Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElse(null);

        if (adminRole == null) {
            return;
        }

        List<Permission> permissions = permissionRepository.findAll();

        adminRole.setPermissions(new HashSet<>(permissions));

        roleRepository.save(adminRole);
    }
}