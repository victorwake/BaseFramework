package com.example.base_framework.controller;

import com.example.base_framework.dto.CreateUserRequest;
import com.example.base_framework.dto.UpdateUserModulesRequest;
import com.example.base_framework.dto.UpdateUserRequest;
import com.example.base_framework.dto.UpdateUserRolesRequest;
import com.example.base_framework.entity.User;
import com.example.base_framework.service.UserService;
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

@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Listar usuarios", description = "Obtiene todos los usuarios con paginación y búsqueda opcional por nombre o email")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de usuarios paginada"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PreAuthorize("hasAuthority('USER_READ')")
    @GetMapping
    public Page<User> getUsers(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return userService.findAllUsersPaged(search, pageable);
    }

    @Operation(summary = "Obtener usuario por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PreAuthorize("hasAuthority('USER_READ')")
    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.findUserById(id);
    }

    @Operation(summary = "Crear usuario", description = "Crea un nuevo usuario con rol ROLE_USER por defecto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PreAuthorize("hasAuthority('USER_CREATE')")
    @PostMapping
    public User createUser(@Valid @RequestBody CreateUserRequest request) {
        return userService.createUser(request);
    }

    @Operation(summary = "Actualizar usuario", description = "Actualiza nombre y email de un usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario actualizado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    @PutMapping("/{id}")
    public User updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return userService.updateUser(id, request);
    }

    @Operation(summary = "Actualizar roles", description = "Actualiza los roles asignados a un usuario")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    @PutMapping("/{id}/roles")
    public void updateUserRoles(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRolesRequest request
    ) {
        userService.updateUserRoles(id, request.getRoles());
    }

    @Operation(summary = "Actualizar módulos", description = "Actualiza los módulos asignados a un usuario")
    @PreAuthorize("hasAuthority('USER_UPDATE')")
    @PutMapping("/{id}/modules")
    public void updateUserModules(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserModulesRequest request
    ) {
        userService.updateUserModules(id, request.getModuleIds());
    }

    @Operation(summary = "Eliminar usuario", description = "Elimina un usuario del sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuario eliminado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PreAuthorize("hasAuthority('USER_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}