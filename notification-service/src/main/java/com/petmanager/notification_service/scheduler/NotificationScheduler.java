package com.petmanager.notification_service.scheduler;

import com.petmanager.notification_service.service.NotificacionPagoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Scheduler para automatizar el procesamiento de notificaciones de vencimiento
 * Se ejecuta automáticamente según la configuración en application.properties
 */
@Component
@Slf4j
@ConditionalOnProperty(
        value = "notifications.scheduler.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class NotificationScheduler {

    @Autowired
    private NotificacionPagoService notificacionPagoService;

    @Value("${notifications.scheduler.enabled:true}")
    private boolean schedulerEnabled;

    @Value("${spring.application.name:notification-service}")
    private String applicationName;

    // ================================================
    // SCHEDULER PRINCIPAL - PROCESAMIENTO DIARIO
    // ================================================

    /**
     * Tarea principal que se ejecuta diariamente a las 8:00 AM
     * Configuración en application.properties: notifications.scheduler.cron=0 0 8 * * ?
     */
    @Scheduled(cron = "${notifications.scheduler.cron:0 0 8 * * ?}")
    public void procesarNotificacionesDiarias() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        log.info("🚀 ===============================================");
        log.info("🚀 INICIO SCHEDULER DIARIO - {}", timestamp);
        log.info("🚀 Servicio: {}", applicationName);
        log.info("🚀 ===============================================");

        if (!schedulerEnabled) {
            log.warn("Scheduler deshabilitado por configuración");
            return;
        }

        try {
            // Verificar conectividad con supplier-service antes de procesar
            boolean supplierDisponible = notificacionPagoService.verificarConectividadSupplierService();

            if (!supplierDisponible) {
                log.error("Supplier-service no disponible. Saltando procesamiento.");
                return;
            }

            log.info("Supplier-service disponible. Iniciando procesamiento...");

            // Ejecutar procesamiento principal
            long startTime = System.currentTimeMillis();
            notificacionPagoService.procesarNotificacionesVencimiento();
            long endTime = System.currentTimeMillis();

            long duracion = endTime - startTime;
            log.info("Procesamiento completado en {} ms ({} segundos)",
                    duracion, String.format("%.2f", duracion / 1000.0));

        } catch (Exception e) {
            log.error("ERROR EN SCHEDULER DIARIO: {}", e.getMessage(), e);

            // Aquí podrías agregar lógica adicional como:
            // - Enviar alerta a administradores
            // - Registrar en sistema de monitoreo
            // - Reintentar el procesamiento

        } finally {
            String endTimestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            log.info("🏁 ===============================================");
            log.info("🏁 FIN SCHEDULER DIARIO - {}", endTimestamp);
            log.info("🏁 ===============================================");
        }
    }

    // ================================================
    // SCHEDULER ADICIONAL - ENVÍO DE PENDIENTES
    // ================================================

    /**
     * Tarea adicional que verifica envío de notificaciones pendientes
     * Se ejecuta cada 2 horas durante horario laboral (8 AM - 6 PM)
     */
    @Scheduled(cron = "0 0 8,10,12,14,16,18 * * MON-FRI")
    public void verificarNotificacionesPendientes() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        log.info("🔍 ===============================================");
        log.info("🔍 VERIFICACIÓN PENDIENTES - {}", timestamp);
        log.info("🔍 ===============================================");

        if (!schedulerEnabled) {
            log.debug("Scheduler deshabilitado por configuración");
            return;
        }

        try {
            long startTime = System.currentTimeMillis();
            notificacionPagoService.enviarNotificacionesPendientes();
            long endTime = System.currentTimeMillis();

            long duracion = endTime - startTime;
            log.info("Verificación de pendientes completada en {} ms", duracion);

        } catch (Exception e) {
            log.error(" ERROR EN VERIFICACIÓN DE PENDIENTES: {}", e.getMessage(), e);
        }
    }

    // ================================================
    // SCHEDULER DE LIMPIEZA - SEMANAL
    // ================================================

    /**
     * Tarea de limpieza que se ejecuta los domingos a las 2 AM
     * Limpia notificaciones muy antiguas y hace mantenimiento
     */
    @Scheduled(cron = "0 0 2 * * SUN")
    public void limpiezaSemanal() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        log.info("🧹 ===============================================");
        log.info("🧹 LIMPIEZA SEMANAL - {}", timestamp);
        log.info("🧹 ===============================================");

        if (!schedulerEnabled) {
            log.debug("Scheduler deshabilitado por configuración");
            return;
        }

        try {
            // lógica de limpieza:
            // - Archivar notificaciones muy antiguas
            // - Limpiar logs
            // - Generar reportes semanales
            // - Verificar integridad de datos

            log.info("Ejecutando limpieza semanal...");

            // Por ahora solo logear
            log.info("Limpieza semanal completada");

        } catch (Exception e) {
            log.error("ERROR EN LIMPIEZA SEMANAL: {}", e.getMessage(), e);
        }
    }


    // MÉTODO PARA TESTING Y DEBUGGING


    /**
     * Método para ejecutar manualmente el procesamiento
     * Útil para testing y debugging
     */
    public void ejecutarProcesamientoManual() {
        log.info("🛠️ EJECUCIÓN MANUAL INICIADA");

        try {
            procesarNotificacionesDiarias();
            log.info("Ejecución manual completada exitosamente");
        } catch (Exception e) {
            log.error("Error en ejecución manual: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Método para verificar el estado del scheduler
     */
    public String obtenerEstadoScheduler() {
        return String.format("""
            📊 ESTADO DEL SCHEDULER:
            ✅ Habilitado: %s
            🏢 Servicio: %s
            ⏰ Próxima ejecución: Todos los días a las 8:00 AM
            🔍 Verificación pendientes: Cada 2 horas (8 AM - 6 PM, Lun-Vie)
            🧹 Limpieza semanal: Domingos a las 2:00 AM
            """,
                schedulerEnabled ? "SÍ" : "NO",
                applicationName
        );
    }

    // ================================================
    // HEALTH CHECK DEL SCHEDULER
    // ================================================

    /**
     * Verifica que el scheduler esté funcionando correctamente
     */
    public boolean verificarSaludScheduler() {
        try {
            // Verificar que el scheduler esté habilitado
            if (!schedulerEnabled) {
                log.warn("Scheduler deshabilitado");
                return false;
            }

            // Verificar conectividad con servicios dependientes
            boolean supplierDisponible = notificacionPagoService.verificarConectividadSupplierService();

            if (!supplierDisponible) {
                log.warn("⚠️ Supplier-service no disponible");
                return false;
            }

            log.info("Scheduler en estado saludable");
            return true;

        } catch (Exception e) {
            log.error("Error verificando salud del scheduler: {}", e.getMessage());
            return false;
        }
    }
}