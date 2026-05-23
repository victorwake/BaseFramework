package com.example.base_framework.controller;

import com.example.base_framework.entity.Permission;
import com.example.base_framework.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @Operation(summary = "Obtener todos los permisos")
    @PreAuthorize("hasAuthority('PERMISSION_READ')")
    @GetMapping
    public List<Permission> getPermissions() {
        return permissionService.getAllPermissions();
    }

    @Operation(summary = "Crear un nuevo permiso")
    @PreAuthorize("hasAuthority('PERMISSION_CREATE')")
    @PostMapping
    public Permission createPermission(@Valid @RequestBody Permission request) {
        return permissionService.createPermission(request);
    }

    @Operation(summary = "Eliminar un permiso")
    @PreAuthorize("hasAuthority('PERMISSION_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePermission(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return ResponseEntity.noContent().build();
    }
}
