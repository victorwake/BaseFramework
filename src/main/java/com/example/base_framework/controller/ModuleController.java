package com.example.base_framework.controller;

import com.example.base_framework.entity.Module;
import com.example.base_framework.service.ModuleService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/modules")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleService moduleService;

    @Operation(summary = "Obtener todos los módulos")
    @PreAuthorize("hasAuthority('MODULE_READ')")
    @GetMapping
    public List<Module> getAll() {
        return moduleService.findAll();
    }

    @Operation(summary = "Obtener módulo por ID")
    @PreAuthorize("hasAuthority('MODULE_READ')")
    @GetMapping("/{id}")
    public Module getById(@PathVariable Long id) {
        return moduleService.findById(id);
    }

    @Operation(summary = "Crear un nuevo módulo")
    @PreAuthorize("hasAuthority('MODULE_CREATE')")
    @PostMapping
    public Module create(@RequestBody Map<String, @NotBlank String> body) {
        return moduleService.create(body.get("name"));
    }

    @Operation(summary = "Actualizar un módulo")
    @PreAuthorize("hasAuthority('MODULE_UPDATE')")
    @PutMapping("/{id}")
    public Module update(
            @PathVariable Long id,
            @RequestBody Map<String, @NotBlank String> body
    ) {
        return moduleService.update(id, body.get("name"));
    }

    @Operation(summary = "Eliminar un módulo")
    @PreAuthorize("hasAuthority('MODULE_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        moduleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
