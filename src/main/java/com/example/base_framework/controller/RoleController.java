package com.example.base_framework.controller;

import com.example.base_framework.entity.Role;
import com.example.base_framework.service.RoleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@Tag(name = "Roles", description = "Gestión de roles del sistema")
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "Listar roles", description = "Obtiene todos los roles con paginación y búsqueda opcional por nombre")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de roles paginada"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PreAuthorize("hasAuthority('ROLE_READ')")
    @GetMapping
    public Page<Role> getRoles(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return roleService.findAllPaged(search, pageable);
    }

    @Operation(summary = "Crear rol", description = "Crea un nuevo rol en el sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rol creado"),
            @ApiResponse(responseCode = "400", description = "El rol ya existe o datos inválidos")
    })
    @PreAuthorize("hasAuthority('ROLE_CREATE')")
    @PostMapping
    public Role createRole(@Valid @RequestBody Role role) {
        return roleService.save(role);
    }

    @Operation(summary = "Actualizar permisos", description = "Actualiza los permisos asignados a un rol")
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    @PutMapping("/{id}/permissions")
    public void updateRolePermissions(
            @PathVariable Long id,
            @RequestBody List<String> permissions
    ) {
        roleService.updatePermissions(id, permissions);
    }

    @Operation(summary = "Eliminar rol", description = "Elimina un rol del sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Rol eliminado"),
            @ApiResponse(responseCode = "404", description = "Rol no encontrado")
    })
    @PreAuthorize("hasAuthority('ROLE_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        roleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}