package com.petmanager.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para el resultado de operaciones de procesamiento
 * Usado en mutations de GraphQL
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultadoProcesamiento {
    private Boolean exitoso;
    private String mensaje;
    private Integer notificacionesCreadas;
    private Integer notificacionesEnviadas;
    private String tiempoEjecucion;
}