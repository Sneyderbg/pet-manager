package com.petmanager.notification_service.service;

import com.petmanager.notification_service.client.SupplierServiceClient;
import com.petmanager.notification_service.dto.CondicionPagoVencimientoDto;
import com.petmanager.notification_service.dto.ProveedorNotificacionDto;
import com.petmanager.notification_service.model.NotificacionPago;
import com.petmanager.notification_service.repository.NotificacionPagoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Servicio principal para la gestión de notificaciones de pago
 * Implementa toda la lógica de negocio del notification-service
 */
@Service
@Slf4j
public class NotificacionPagoService {

    @Autowired
    private SupplierServiceClient supplierServiceClient;

    @Autowired
    private NotificacionPagoRepository notificacionPagoRepository;

    @Autowired
    private EmailService emailService;

    @Value("${notifications.dias-alerta:7,3,1,0}")
    private String diasAlertaConfig;

    @Value("${notifications.max-intentos:3}")
    private int maxIntentos;

    // ================================================
    // MÉTODO PRINCIPAL - PROCESAMIENTO DE NOTIFICACIONES
    // ================================================

    /**
     * Método principal que ejecuta el flujo completo de notificaciones
     */
    @Transactional
    public void procesarNotificacionesVencimiento() {
        log.info("🚀 Iniciando procesamiento de notificaciones de vencimiento...");

        try {
            // Paso 1: Obtener condiciones próximas a vencer desde supplier-service
            List<CondicionPagoVencimientoDto> condicionesProximas = obtenerCondicionesProximasAVencer();
            log.info("📋 Encontradas {} condiciones próximas a vencer", condicionesProximas.size());

            // Paso 2: Procesar cada condición
            List<NotificacionPago> notificacionesCreadas = new ArrayList<>();
            for (CondicionPagoVencimientoDto condicion : condicionesProximas) {
                try {
                    NotificacionPago notificacion = procesarCondicionIndividual(condicion);
                    if (notificacion != null) {
                        notificacionesCreadas.add(notificacion);
                    }
                } catch (Exception e) {
                    log.error("❌ Error procesando condición ID {}: {}",
                            condicion.getIdCondicionPago(), e.getMessage());
                }
            }

            log.info("✅ Procesamiento completado. Notificaciones creadas: {}", notificacionesCreadas.size());

            // Paso 3: Enviar notificaciones pendientes
            enviarNotificacionesPendientes();

        } catch (Exception e) {
            log.error("💥 Error en procesamiento de notificaciones: {}", e.getMessage(), e);
            throw new RuntimeException("Error procesando notificaciones de vencimiento", e);
        }
    }

    /**
     * Procesa una condición individual y crea notificación si es necesario
     */
    private NotificacionPago procesarCondicionIndividual(CondicionPagoVencimientoDto condicion) {
        log.debug("🔍 Procesando condición ID: {} - Proveedor: {} - Días restantes: {}",
                condicion.getIdCondicionPago(), condicion.getNombreProveedor(), condicion.getDiasRestantes());

        // Verificar si ya existe notificación para esta condición
        Optional<NotificacionPago> notificacionExistente = notificacionPagoRepository
                .findByIdProveedorAndIdCondicionPago(
                        condicion.getIdProveedor().intValue(),
                        condicion.getIdCondicionPago().intValue()
                );

        if (notificacionExistente.isPresent()) {
            log.debug("⚠️ Ya existe notificación para condición ID: {}", condicion.getIdCondicionPago());
            // Actualizar campos transient y retornar existente
            return enriquecerNotificacion(notificacionExistente.get(), condicion);
        }

        // Verificar si debe crear notificación
        if (!condicion.debeCrearNotificacion()) {
            log.debug("🚫 No debe crear notificación para condición ID: {} (días restantes: {})",
                    condicion.getIdCondicionPago(), condicion.getDiasRestantes());
            return null;
        }

        // Crear nueva notificación
        return crearNuevaNotificacion(condicion);
    }

    /**
     * Crea una nueva notificación basada en la condición de pago
     */
    private NotificacionPago crearNuevaNotificacion(CondicionPagoVencimientoDto condicion) {
        log.info("✨ Creando nueva notificación para condición ID: {} - Proveedor: {}",
                condicion.getIdCondicionPago(), condicion.getNombreProveedor());

        NotificacionPago notificacion = NotificacionPago.builder()
                .idProveedor(condicion.getIdProveedor().intValue())
                .idCondicionPago(condicion.getIdCondicionPago().intValue())
                .fechaVencimiento(condicion.calcularFechaVencimiento())
                .fechaNotificacion(LocalDateTime.now())
                .notificado(false)
                .estado("Pendiente")
                .build();

        // Enriquecer con datos transient
        notificacion = enriquecerNotificacion(notificacion, condicion);

        // Guardar en BD
        NotificacionPago notificacionGuardada = notificacionPagoRepository.save(notificacion);
        log.info("💾 Notificación creada con ID: {}", notificacionGuardada.getIdNotificacionPago());

        return notificacionGuardada;
    }

    /**
     * Enriquece la notificación con datos transient del supplier-service
     */
    private NotificacionPago enriquecerNotificacion(NotificacionPago notificacion, CondicionPagoVencimientoDto condicion) {
        // Campos transient (no se guardan en BD)
        notificacion.setNombreProveedor(condicion.getNombreProveedor());
        notificacion.setEmailProveedor(condicion.getEmailProveedor());
        notificacion.setDiasCredito(condicion.getDiasCredito());
        notificacion.setDiasRestantes(condicion.getDiasRestantes());
        notificacion.setTipoNotificacion(condicion.determinarTipoNotificacion());
        notificacion.setNota(condicion.getNota());

        return notificacion;
    }

    // ================================================
    // ENVÍO DE NOTIFICACIONES
    // ================================================

    /**
     * Envía todas las notificaciones pendientes
     */
    @Transactional
    public void enviarNotificacionesPendientes() {
        log.info("📧 Iniciando envío de notificaciones pendientes...");

        // Por ahora, enviar TODAS las notificaciones pendientes (simplificado)
        List<NotificacionPago> notificacionesPendientes = notificacionPagoRepository.findByNotificadoFalse();

        log.info("📬 Encontradas {} notificaciones pendientes para enviar", notificacionesPendientes.size());

        int exitosos = 0;
        int fallidos = 0;

        for (NotificacionPago notificacion : notificacionesPendientes) {
            try {
                // Enriquecer notificación con datos del supplier-service
                NotificacionPago notificacionEnriquecida = enriquecerNotificacionPublico(notificacion);

                // Enviar email
                boolean enviado = emailService.enviarNotificacionVencimiento(notificacionEnriquecida);

                if (enviado) {
                    // Marcar como enviada
                    notificacion.setNotificado(true);
                    notificacion.setEstado("Enviada");
                    notificacionPagoRepository.save(notificacion);
                    exitosos++;
                    log.info("✅ Notificación enviada: ID {} - Proveedor: {}",
                            notificacion.getIdNotificacionPago(), notificacionEnriquecida.getNombreProveedor());
                } else {
                    fallidos++;
                    log.warn("⚠️ Falló envío de notificación ID: {}", notificacion.getIdNotificacionPago());
                }

            } catch (Exception e) {
                fallidos++;
                log.error("❌ Error enviando notificación ID {}: {}",
                        notificacion.getIdNotificacionPago(), e.getMessage());
            }
        }

        log.info("📊 Envío completado - Exitosos: {} | Fallidos: {}", exitosos, fallidos);
    }

    // ================================================
    // MÉTODOS PÚBLICOS PARA GRAPHQL RESOLVER
    // ================================================

    /**
     * Enriquece una notificación existente con datos del supplier-service
     * (método público para usar desde el resolver)
     */
    public NotificacionPago enriquecerNotificacionPublico(NotificacionPago notificacion) {
        try {
            // Obtener información del proveedor
            ResponseEntity<ProveedorNotificacionDto> response =
                    supplierServiceClient.obtenerInfoProveedor(notificacion.getIdProveedor().longValue());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                var proveedor = response.getBody();
                notificacion.setNombreProveedor(proveedor.getNombre());
                notificacion.setEmailProveedor(proveedor.getEmail());
            }

            // Calcular días restantes y tipo
            notificacion.setDiasRestantes(notificacion.calcularDiasRestantes());
            notificacion.setTipoNotificacion(notificacion.determinarTipoNotificacion());

        } catch (Exception e) {
            log.warn("⚠️ No se pudo enriquecer notificación ID {}: {}",
                    notificacion.getIdNotificacionPago(), e.getMessage());

            // Valores por defecto si falla la conexión
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

    /**
     * Obtiene todas las notificaciones con información enriquecida
     */
    public List<NotificacionPago> obtenerTodasLasNotificaciones() {
        List<NotificacionPago> notificaciones = notificacionPagoRepository.findAllByOrderByFechaVencimientoAsc();

        // Enriquecer cada notificación
        return notificaciones.stream()
                .map(this::enriquecerNotificacionPublico)
                .toList();
    }

    /**
     * Obtiene notificaciones por proveedor
     */
    public List<NotificacionPago> obtenerNotificacionesPorProveedor(Integer idProveedor) {
        List<NotificacionPago> notificaciones =
                notificacionPagoRepository.findByIdProveedorOrderByFechaVencimientoAsc(idProveedor);

        return notificaciones.stream()
                .map(this::enriquecerNotificacionPublico)
                .toList();
    }

    // ================================================
    // MÉTODOS DE UTILIDAD
    // ================================================

    /**
     * Obtiene condiciones próximas a vencer desde supplier-service
     */
    private List<CondicionPagoVencimientoDto> obtenerCondicionesProximasAVencer() {
        try {
            ResponseEntity<List<CondicionPagoVencimientoDto>> response =
                    supplierServiceClient.obtenerCondicionesProximasAVencer(diasAlertaConfig);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                log.warn("⚠️ Supplier-service devolvió estado: {}", response.getStatusCode());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("💥 Error consultando supplier-service: {}", e.getMessage());
            throw new RuntimeException("Error conectando con supplier-service", e);
        }
    }

    /**
     * Parsea la configuración de días de alerta
     */
    private List<Integer> parsearDiasAlerta() {
        try {
            String[] diasArray = diasAlertaConfig.split(",");
            List<Integer> dias = new ArrayList<>();
            for (String dia : diasArray) {
                dias.add(Integer.parseInt(dia.trim()));
            }
            return dias;
        } catch (Exception e) {
            log.warn("⚠️ Error parseando días de alerta, usando default: {}", e.getMessage());
            return List.of(7, 3, 1, 0); // Default
        }
    }

    /**
     * Verifica conectividad con supplier-service
     */
    public boolean verificarConectividadSupplierService() {
        try {
            ResponseEntity<String> response = supplierServiceClient.healthCheck();
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("❌ Error verificando supplier-service: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Ejecuta procesamiento manual (para testing)
     */
    public String ejecutarProcesamiento() {
        try {
            procesarNotificacionesVencimiento();
            return "✅ Procesamiento ejecutado exitosamente";
        } catch (Exception e) {
            return "❌ Error en procesamiento: " + e.getMessage();
        }
    }

    /**
     * Getter para el SupplierServiceClient (usado por NotificacionResolver)
     */
    public SupplierServiceClient getSupplierServiceClient() {
        return supplierServiceClient;
    }
}