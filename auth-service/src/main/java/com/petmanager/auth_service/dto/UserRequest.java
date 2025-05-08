package com.petmanager.auth_service.dto;

import lombok.Data;

@Data
public class UserRequest {
    private String nombre;
    private String email;
    private String password;
}
