package com.petmanager.notification_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que mapea la tabla notificacion_pago existente en tu BD universitaria
 * Respeta completamente el esquema original
 */
@Entity
@Table(name = "notificacion_pago")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificacionPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacion_pago")
    private Integer idNotificacionPago;

    @Column(name = "id_proveedor", nullable = false)
    private Integer idProveedor;

    @Column(name = "id_condicion_pago", nullable = false)
    private Integer idCondicionPago;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Column(name = "fecha_notificacion")
    private LocalDateTime fechaNotificacion;

    @Column(name = "notificado")
    private Boolean notificado = false;

    @Column(name = "estado", length = 20)
    private String estado = "Pendiente";

    // ================================================
    // CAMPOS TRANSIENT PARA FUNCIONALIDAD EXTENDIDA
    // (No están en BD, solo en memoria para lógica)
    // ================================================

    @Transient
    private String nombreProveedor;

    @Transient
    private String emailProveedor;

    @Transient
    private Integer diasCredito;

    @Transient
    private Integer diasRestantes;

    @Transient
    private String tipoNotificacion; // PROXIMO, INMINENTE, HOY, VENCIDO

    @Transient
    private String nota; // De la condición de pago

    // ================================================
    // MÉTODOS HELPER
    // ================================================

    /**
     * Calcula los días restantes hasta el vencimiento
     */
    public int calcularDiasRestantes() {
        if (fechaVencimiento == null) return 0;
        return (int) java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), fechaVencimiento);
    }

    /**
     * Determina el tipo de notificación según días restantes
     */
    public String determinarTipoNotificacion() {
        int dias = calcularDiasRestantes();

        if (dias >= 7) return "VENCIMIENTO_PROXIMO";
        if (dias >= 3) return "VENCIMIENTO_INMINENTE";
        if (dias >= 0) return "VENCIMIENTO_HOY";
        return "VENCIDO";
    }

    /**
     * Verifica si la notificación está vencida
     */
    public boolean estaVencida() {
        return fechaVencimiento != null && fechaVencimiento.isBefore(LocalDate.now());
    }

    /**
     * Verifica si debe enviarse notificación hoy
     */
    public boolean debeNotificarseHoy(int[] diasAlerta) {
        int diasRestantes = calcularDiasRestantes();

        for (int dia : diasAlerta) {
            if (diasRestantes == dia) {
                return true;
            }
        }
        return false;
    }
}