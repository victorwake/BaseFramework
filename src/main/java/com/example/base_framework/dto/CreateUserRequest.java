package com.example.base_framework.dto;

import lombok.Data;

@Data
public class CreateUserRequest {

    private String name;
    private String email;
    private String password;

}