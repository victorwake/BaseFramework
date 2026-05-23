package com.example.base_framework.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class UpdateUserModulesRequest {

    @NotEmpty(message = "Debe asignar al menos un módulo")
    private List<Long> moduleIds;
}
