package com.example.base_framework.service;

import com.example.base_framework.entity.Permission;
import com.example.base_framework.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    public Permission createPermission(Permission request) {

        if (permissionRepository.findByName(request.getName()).isPresent()) {
            throw new RuntimeException("Permission already exists");
        }

        return permissionRepository.save(request);
    }

    public void deletePermission(Long id) {
        permissionRepository.deleteById(id);
    }
}