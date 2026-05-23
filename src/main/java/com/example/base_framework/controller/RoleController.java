package com.example.base_framework.controller;

import com.example.base_framework.entity.Role;
import com.example.base_framework.service.RoleService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "Obtener todos los roles (con paginación y búsqueda)")
    @PreAuthorize("hasAuthority('ROLE_READ')")
    @GetMapping
    public Page<Role> getRoles(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return roleService.findAllPaged(search, pageable);
    }

    @Operation(summary = "Crear un nuevo rol")
    @PreAuthorize("hasAuthority('ROLE_CREATE')")
    @PostMapping
    public Role createRole(@Valid @RequestBody Role role) {
        return roleService.save(role);
    }

    @Operation(summary = "Actualizar permisos de un rol")
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    @PutMapping("/{id}/permissions")
    public void updateRolePermissions(
            @PathVariable Long id,
            @RequestBody List<String> permissions
    ) {
        roleService.updatePermissions(id, permissions);
    }

    @Operation(summary = "Eliminar un rol")
    @PreAuthorize("hasAuthority('ROLE_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        roleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}