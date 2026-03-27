package com.example.base_framework.controller;

import com.example.base_framework.entity.Role;
import com.example.base_framework.service.RoleService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PreAuthorize("hasAuthority('ROLE_READ')")
    @GetMapping
    public List<Role> getRoles() {
        return roleService.findAll();
    }

    @PreAuthorize("hasAuthority('ROLE_CREATE')")
    @PostMapping
    public Role createRole(@RequestBody Role role) {
        return roleService.save(role);
    }

    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    @PutMapping("/{id}/permissions")
    public void updateRolePermissions(
            @PathVariable Long id,
            @RequestBody List<String> permissions
    ) {
        roleService.updatePermissions(id, permissions);
    }

    @PreAuthorize("hasAuthority('ROLE_DELETE')")
    @DeleteMapping("/{id}")
    public void deleteRole(@PathVariable Long id) {
        roleService.delete(id);
    }
}