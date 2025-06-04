package com.petmanager.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para información básica del proveedor
 * Usado para notificaciones específicas
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProveedorNotificacionDto {
    private Long idProveedor;
    private String nombre;
    private String email;
    private Boolean activo;

    /**
     * Verifica si el proveedor puede recibir notificaciones
     */
    public boolean puedeRecibirNotificaciones() {
        return activo != null && activo &&
                email != null && !email.trim().isEmpty();
    }
}