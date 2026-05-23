package com.example.base_framework.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class UpdateUserRolesRequest {

    @NotEmpty(message = "Debe asignar al menos un rol")
    private List<String> roles;

}