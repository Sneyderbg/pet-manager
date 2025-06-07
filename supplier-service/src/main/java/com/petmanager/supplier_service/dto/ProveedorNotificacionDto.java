package com.petmanager.supplier_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProveedorNotificacionDto {
    private Long idProveedor;
    private String nombre;
    private String email;
    private Boolean activo;
}