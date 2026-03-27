package com.example.base_framework.service;

import com.example.base_framework.entity.Role;
import com.example.base_framework.entity.Permission;
import com.example.base_framework.repository.RoleRepository;
import com.example.base_framework.repository.PermissionRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    public Role save(Role role) {

        if (roleRepository.findByName(role.getName()).isPresent()) {
            throw new RuntimeException("Role already exists");
        }

        return roleRepository.save(role);
    }

    public void delete(Long id) {
        roleRepository.deleteById(id);
    }

    public void updatePermissions(Long roleId, List<String> permissionNames) {

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        List<Permission> permissions = permissionRepository.findByNameIn(permissionNames);

        role.setPermissions(new HashSet<>(permissions));

        roleRepository.save(role);
    }
}