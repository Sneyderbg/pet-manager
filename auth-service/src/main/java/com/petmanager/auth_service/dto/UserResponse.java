package com.petmanager.auth_service.dto;

import com.petmanager.auth_service.model.Rol;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String nombre;
    private String email;
    private boolean activo;
    private List<Rol> roles;
}
