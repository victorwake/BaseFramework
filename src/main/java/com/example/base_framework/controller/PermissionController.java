package com.example.base_framework.controller;

import com.example.base_framework.entity.Permission;
import com.example.base_framework.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Permisos", description = "Gestión de permisos del sistema")
@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @Operation(summary = "Listar permisos", description = "Obtiene todos los permisos del sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de permisos"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PreAuthorize("hasAuthority('PERMISSION_READ')")
    @GetMapping
    public List<Permission> getPermissions() {
        return permissionService.getAllPermissions();
    }

    @Operation(summary = "Crear permiso", description = "Crea un nuevo permiso en el sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Permiso creado"),
            @ApiResponse(responseCode = "400", description = "El permiso ya existe o datos inválidos")
    })
    @PreAuthorize("hasAuthority('PERMISSION_CREATE')")
    @PostMapping
    public Permission createPermission(@Valid @RequestBody Permission request) {
        return permissionService.createPermission(request);
    }

    @Operation(summary = "Eliminar permiso", description = "Elimina un permiso del sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Permiso eliminado"),
            @ApiResponse(responseCode = "404", description = "Permiso no encontrado")
    })
    @PreAuthorize("hasAuthority('PERMISSION_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePermission(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return ResponseEntity.noContent().build();
    }
}
