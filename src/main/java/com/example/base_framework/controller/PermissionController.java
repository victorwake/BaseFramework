package com.example.base_framework.controller;

import com.example.base_framework.entity.Permission;
import com.example.base_framework.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @PreAuthorize("hasAuthority('ROLE_READ')")
    @GetMapping
    public List<Permission> getPermissions() {
        return permissionService.getAllPermissions();
    }

    @PreAuthorize("hasAuthority('ROLE_CREATE')")
    @PostMapping
    public Permission createPermission(@Valid @RequestBody Permission request) {
        return permissionService.createPermission(request);
    }

    @PreAuthorize("hasAuthority('ROLE_DELETE')")
    @DeleteMapping("/{id}")
    public void deletePermission(@PathVariable Long id) {
        permissionService.deletePermission(id);
    }
}
