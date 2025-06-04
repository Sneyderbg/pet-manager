package com.petmanager.notification_service.repository;

import com.petmanager.notification_service.model.NotificacionPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository para gestionar operaciones CRUD en la tabla notificacion_pago
 */
@Repository
public interface NotificacionPagoRepository extends JpaRepository<NotificacionPago, Integer> {

    /**
     * Buscar notificaciones por proveedor
     */
    List<NotificacionPago> findByIdProveedor(Integer idProveedor);

    /**
     * Buscar notificaciones por condición de pago
     */
    List<NotificacionPago> findByIdCondicionPago(Integer idCondicionPago);

    /**
     * Buscar notificaciones por estado
     */
    List<NotificacionPago> findByEstado(String estado);

    /**
     * Buscar notificaciones no enviadas (notificado = false)
     */
    List<NotificacionPago> findByNotificadoFalse();

    /**
     * Buscar notificaciones pendientes
     */
    List<NotificacionPago> findByEstadoAndNotificadoFalse(String estado);

    /**
     * Buscar notificaciones que vencen en una fecha específica
     */
    List<NotificacionPago> findByFechaVencimiento(LocalDate fechaVencimiento);

    /**
     * Buscar notificaciones que vencen en un rango de fechas
     */
    List<NotificacionPago> findByFechaVencimientoBetween(LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Buscar notificaciones que vencen hoy o antes (vencidas)
     */
    @Query("SELECT n FROM NotificacionPago n WHERE n.fechaVencimiento <= :fecha")
    List<NotificacionPago> findVencidas(@Param("fecha") LocalDate fecha);

    /**
     * Buscar notificaciones próximas a vencer (en los próximos X días)
     */
    @Query("SELECT n FROM NotificacionPago n WHERE n.fechaVencimiento BETWEEN :hoy AND :fechaLimite")
    List<NotificacionPago> findProximasAVencer(@Param("hoy") LocalDate hoy,
                                               @Param("fechaLimite") LocalDate fechaLimite);

    /**
     * Buscar si ya existe una notificación para una condición de pago específica
     */
    Optional<NotificacionPago> findByIdProveedorAndIdCondicionPago(Integer idProveedor, Integer idCondicionPago);

    /**
     * Verificar si ya existe notificación para una condición de pago
     */
    boolean existsByIdProveedorAndIdCondicionPago(Integer idProveedor, Integer idCondicionPago);

    /**
     * Contar notificaciones pendientes por proveedor
     */
    @Query("SELECT COUNT(n) FROM NotificacionPago n WHERE n.idProveedor = :idProveedor AND n.notificado = false")
    Long countNotificacionesPendientesByProveedor(@Param("idProveedor") Integer idProveedor);

    /**
     * Buscar notificaciones que deben enviarse hoy
     * (aquellas cuyo vencimiento coincide con los días de alerta configurados)
     */
    @Query("SELECT n FROM NotificacionPago n WHERE " +
            "n.notificado = false AND " +
            "n.estado = 'Pendiente' AND " +
            "(FUNCTION('DATE_PART', 'day', n.fechaVencimiento - CURRENT_DATE) IN :diasAlerta)")
    List<NotificacionPago> findNotificacionesParaEnviarHoy(@Param("diasAlerta") List<Integer> diasAlerta);

    /**
     * Buscar notificaciones por fecha de vencimiento específica y estado
     */
    List<NotificacionPago> findByFechaVencimientoAndEstado(LocalDate fechaVencimiento, String estado);

    /**
     * Obtener todas las notificaciones ordenadas por fecha de vencimiento
     */
    List<NotificacionPago> findAllByOrderByFechaVencimientoAsc();

    /**
     * Buscar notificaciones de un proveedor ordenadas por fecha de vencimiento
     */
    List<NotificacionPago> findByIdProveedorOrderByFechaVencimientoAsc(Integer idProveedor);
}