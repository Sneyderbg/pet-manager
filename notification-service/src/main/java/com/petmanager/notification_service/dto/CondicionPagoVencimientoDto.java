package com.petmanager.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO que mapea la respuesta del supplier-service
 * Contiene toda la información necesaria para crear notificaciones
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CondicionPagoVencimientoDto {
    private Long idCondicionPago;
    private Long idProveedor;
    private String nombreProveedor;
    private String emailProveedor;
    private Long idUsuario;
    private Integer diasCredito;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String nota;
    private Integer diasRestantes;

    /**
     * Calcula la fecha de vencimiento basada en fechaFin
     */
    public LocalDate calcularFechaVencimiento() {
        return fechaFin;
    }

    /**
     * Determina el tipo de notificación según días restantes
     */
    public String determinarTipoNotificacion() {
        if (diasRestantes == null) return "DESCONOCIDO";

        if (diasRestantes >= 7) return "VENCIMIENTO_PROXIMO";
        if (diasRestantes >= 3) return "VENCIMIENTO_INMINENTE";
        if (diasRestantes >= 0) return "VENCIMIENTO_HOY";
        return "VENCIDO";
    }

    /**
     * Verifica si debe crear notificación
     */
    public boolean debeCrearNotificacion() {
        return diasRestantes != null && diasRestantes >= -7; // Hasta 7 días después
    }
}