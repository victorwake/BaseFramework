package com.example.base_framework.controller;

import com.example.base_framework.entity.Module;
import com.example.base_framework.service.ModuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Módulos", description = "Gestión de módulos funcionales del sistema")
@RestController
@RequestMapping("/api/v1/modules")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleService moduleService;

    @Operation(summary = "Listar módulos", description = "Obtiene todos los módulos funcionales")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de módulos"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PreAuthorize("hasAuthority('MODULE_READ')")
    @GetMapping
    public List<Module> getAll() {
        return moduleService.findAll();
    }

    @Operation(summary = "Obtener módulo por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Módulo encontrado"),
            @ApiResponse(responseCode = "404", description = "Módulo no encontrado")
    })
    @PreAuthorize("hasAuthority('MODULE_READ')")
    @GetMapping("/{id}")
    public Module getById(@PathVariable Long id) {
        return moduleService.findById(id);
    }

    @Operation(summary = "Crear módulo", description = "Crea un nuevo módulo funcional")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Módulo creado"),
            @ApiResponse(responseCode = "400", description = "El módulo ya existe o nombre inválido")
    })
    @PreAuthorize("hasAuthority('MODULE_CREATE')")
    @PostMapping
    public Module create(@RequestBody Map<String, @NotBlank String> body) {
        return moduleService.create(body.get("name"));
    }

    @Operation(summary = "Actualizar módulo", description = "Actualiza el nombre de un módulo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Módulo actualizado"),
            @ApiResponse(responseCode = "404", description = "Módulo no encontrado")
    })
    @PreAuthorize("hasAuthority('MODULE_UPDATE')")
    @PutMapping("/{id}")
    public Module update(
            @PathVariable Long id,
            @RequestBody Map<String, @NotBlank String> body
    ) {
        return moduleService.update(id, body.get("name"));
    }

    @Operation(summary = "Eliminar módulo", description = "Elimina un módulo del sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Módulo eliminado"),
            @ApiResponse(responseCode = "404", description = "Módulo no encontrado")
    })
    @PreAuthorize("hasAuthority('MODULE_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        moduleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
