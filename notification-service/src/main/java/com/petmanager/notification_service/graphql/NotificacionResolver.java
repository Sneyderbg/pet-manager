package com.petmanager.notification_service.graphql;

import com.petmanager.notification_service.dto.ResultadoProcesamiento;
import com.petmanager.notification_service.model.NotificacionPago;
import com.petmanager.notification_service.service.NotificacionPagoService;
import com.petmanager.notification_service.repository.NotificacionPagoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * GraphQL Resolver simplificado para NotificacionPago
 * Enfocado en funcionalidades esenciales del proyecto
 */
@Controller
@Slf4j
public class NotificacionResolver {

    @Autowired
    private NotificacionPagoService notificacionPagoService;

    @Autowired
    private NotificacionPagoRepository notificacionPagoRepository;

    // ================================================
    // QUERIES - Consultas esenciales
    // ================================================

    @QueryMapping
    public List<NotificacionPago> getNotificaciones() {
        log.info("🔍 GraphQL Query: getNotificaciones");
        return notificacionPagoService.obtenerTodasLasNotificaciones();
    }

    @QueryMapping
    public NotificacionPago getNotificacionById(@Argument String id) {
        log.info("🔍 GraphQL Query: getNotificacionById - ID: {}", id);

        Optional<NotificacionPago> notificacion = notificacionPagoRepository.findById(Integer.parseInt(id));

        if (notificacion.isPresent()) {
            return notificacionPagoService.enriquecerNotificacionPublico(notificacion.get());
        }

        log.warn("⚠️ Notificación no encontrada con ID: {}", id);
        return null;
    }

    @QueryMapping
    public List<NotificacionPago> getNotificacionesByProveedor(@Argument Integer idProveedor) {
        log.info("🔍 GraphQL Query: getNotificacionesByProveedor - Proveedor: {}", idProveedor);
        return notificacionPagoService.obtenerNotificacionesPorProveedor(idProveedor);
    }

    @QueryMapping
    public List<NotificacionPago> getNotificacionesPendientes() {
        log.info("🔍 GraphQL Query: getNotificacionesPendientes");

        List<NotificacionPago> pendientes = notificacionPagoRepository.findByNotificadoFalse();

        return pendientes.stream()
                .map(notificacion -> notificacionPagoService.enriquecerNotificacionPublico(notificacion))
                .toList();
    }

    @QueryMapping
    public String healthCheck() {
        log.info("🔍 GraphQL Query: healthCheck");
        return "✅ Notification Service GraphQL API - " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // ================================================
    // MUTATIONS - Operaciones esenciales
    // ================================================

    @MutationMapping
    public ResultadoProcesamiento ejecutarProcesamientoManual() {
        log.info("🚀 GraphQL Mutation: ejecutarProcesamientoManual");

        try {
            long startTime = System.currentTimeMillis();
            String resultado = notificacionPagoService.ejecutarProcesamiento();
            long endTime = System.currentTimeMillis();

            String tiempoEjecucion = String.format("%.2f segundos", (endTime - startTime) / 1000.0);

            return ResultadoProcesamiento.builder()
                    .exitoso(true)
                    .mensaje(resultado)
                    .tiempoEjecucion(tiempoEjecucion)
                    .build();

        } catch (Exception e) {
            log.error("❌ Error en procesamiento manual: {}", e.getMessage());
            return ResultadoProcesamiento.builder()
                    .exitoso(false)
                    .mensaje("Error en procesamiento: " + e.getMessage())
                    .build();
        }
    }

    @MutationMapping
    public ResultadoProcesamiento enviarNotificacionesPendientes() {
        log.info("📧 GraphQL Mutation: enviarNotificacionesPendientes");

        try {
            long startTime = System.currentTimeMillis();
            notificacionPagoService.enviarNotificacionesPendientes();
            long endTime = System.currentTimeMillis();

            String tiempoEjecucion = String.format("%.2f segundos", (endTime - startTime) / 1000.0);

            return ResultadoProcesamiento.builder()
                    .exitoso(true)
                    .mensaje("✅ Notificaciones pendientes enviadas exitosamente")
                    .tiempoEjecucion(tiempoEjecucion)
                    .build();

        } catch (Exception e) {
            log.error("❌ Error enviando notificaciones pendientes: {}", e.getMessage());
            return ResultadoProcesamiento.builder()
                    .exitoso(false)
                    .mensaje("Error enviando notificaciones: " + e.getMessage())
                    .build();
        }
    }

    @MutationMapping
    public NotificacionPago marcarNotificacionComoEnviada(@Argument String id) {
        log.info("✅ GraphQL Mutation: marcarNotificacionComoEnviada - ID: {}", id);

        try {
            Optional<NotificacionPago> optional = notificacionPagoRepository.findById(Integer.parseInt(id));

            if (optional.isPresent()) {
                NotificacionPago notificacion = optional.get();
                notificacion.setNotificado(true);
                notificacion.setEstado("Enviada");

                NotificacionPago guardada = notificacionPagoRepository.save(notificacion);
                log.info("✅ Notificación marcada como enviada: ID {}", id);

                return notificacionPagoService.enriquecerNotificacionPublico(guardada);
            } else {
                log.warn("⚠️ Notificación no encontrada con ID: {}", id);
                return null;
            }
        } catch (Exception e) {
            log.error("❌ Error marcando notificación como enviada: {}", e.getMessage());
            return null;
        }
    }

    @MutationMapping
    public ResultadoProcesamiento sincronizarConSupplierService() {
        log.info("🔄 GraphQL Mutation: sincronizarConSupplierService");

        try {
            // Verificar conectividad
            boolean disponible = notificacionPagoService.verificarConectividadSupplierService();

            if (!disponible) {
                return ResultadoProcesamiento.builder()
                        .exitoso(false)
                        .mensaje("❌ Supplier-service no disponible")
                        .build();
            }

            // Ejecutar sincronización
            String resultado = notificacionPagoService.ejecutarProcesamiento();

            return ResultadoProcesamiento.builder()
                    .exitoso(true)
                    .mensaje("✅ Sincronización completada: " + resultado)
                    .build();

        } catch (Exception e) {
            log.error("❌ Error en sincronización: {}", e.getMessage());
            return ResultadoProcesamiento.builder()
                    .exitoso(false)
                    .mensaje("Error en sincronización: " + e.getMessage())
                    .build();
        }
    }

    // ================================================
    // MÉTODO DE UTILIDAD
    // ================================================

    /**
     * Enriquece una notificación con datos del supplier-service
     */
    private NotificacionPago enriquecerNotificacion(NotificacionPago notificacion) {
        try {
            // Obtener información del proveedor desde supplier-service
            var response = notificacionPagoService.getSupplierServiceClient()
                    .obtenerInfoProveedor(notificacion.getIdProveedor().longValue());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                var proveedor = response.getBody();
                notificacion.setNombreProveedor(proveedor.getNombre());
                notificacion.setEmailProveedor(proveedor.getEmail());
            }

            // Calcular campos transient
            notificacion.setDiasRestantes(notificacion.calcularDiasRestantes());
            notificacion.setTipoNotificacion(notificacion.determinarTipoNotificacion());

        } catch (Exception e) {
            log.debug("⚠️ No se pudo enriquecer notificación ID {}: {}",
                    notificacion.getIdNotificacionPago(), e.getMessage());

            // Valores por defecto
            if (notificacion.getNombreProveedor() == null) {
                notificacion.setNombreProveedor("Proveedor " + notificacion.getIdProveedor());
            }
            if (notificacion.getEmailProveedor() == null) {
                notificacion.setEmailProveedor("sin-email@example.com");
            }

            notificacion.setDiasRestantes(notificacion.calcularDiasRestantes());
            notificacion.setTipoNotificacion(notificacion.determinarTipoNotificacion());
        }

        return notificacion;
    }
}