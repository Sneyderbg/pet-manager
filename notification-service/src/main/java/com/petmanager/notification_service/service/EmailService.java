package com.petmanager.notification_service.service;

import com.petmanager.notification_service.model.NotificacionPago;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * Servicio para envío de notificaciones por email
 * Implementación básica con logs (después configuraremos Brevo)
 */
@Service
@Slf4j
public class EmailService {

    @Value("${brevo.sender.email:notificaciones@petmanager.com}")
    private String senderEmail;

    @Value("${brevo.sender.name:PetManager Notificaciones}")
    private String senderName;

    @Value("${notifications.max-intentos:3}")
    private int maxIntentos;

    /**
     * Envía notificación de vencimiento de condición de pago
     * Por ahora solo logea, después implementaremos Brevo
     */
    public boolean enviarNotificacionVencimiento(NotificacionPago notificacion) {
        try {
            // Generar contenido del email
            String asunto = generarAsunto(notificacion);
            String contenido = generarContenidoEmail(notificacion);
            String destinatario = notificacion.getEmailProveedor();

            // Por ahora solo logeamos (después implementaremos Brevo)
            log.info("📧 ENVIANDO EMAIL:");
            log.info("   📨 Para: {} ({})", notificacion.getNombreProveedor(), destinatario);
            log.info("   📋 Asunto: {}", asunto);
            log.info("   📄 Contenido:\n{}", contenido);
            log.info("   🕒 Tipo: {}", notificacion.getTipoNotificacion());
            log.info("   📅 Vencimiento: {}", notificacion.getFechaVencimiento());

            // Simular envío exitoso
            Thread.sleep(100); // Simular latencia de envío

            log.info("✅ Email enviado exitosamente a {}", destinatario);
            return true;

        } catch (Exception e) {
            log.error("❌ Error enviando email a {}: {}",
                    notificacion.getEmailProveedor(), e.getMessage());
            return false;
        }
    }

    /**
     * Genera el asunto del email según el tipo de notificación
     */
    private String generarAsunto(NotificacionPago notificacion) {
        String tipoNotificacion = notificacion.getTipoNotificacion();
        String nombreProveedor = notificacion.getNombreProveedor();

        return switch (tipoNotificacion) {
            case "VENCIMIENTO_PROXIMO" ->
                    String.format("🔔 Recordatorio: Condición de pago próxima a vencer - %s", nombreProveedor);
            case "VENCIMIENTO_INMINENTE" ->
                    String.format("⚠️ URGENTE: Condición de pago vence pronto - %s", nombreProveedor);
            case "VENCIMIENTO_HOY" ->
                    String.format("🚨 CRÍTICO: Condición de pago vence HOY - %s", nombreProveedor);
            case "VENCIDO" ->
                    String.format("❌ VENCIDO: Condición de pago expirada - %s", nombreProveedor);
            default ->
                    String.format("📋 Notificación de condición de pago - %s", nombreProveedor);
        };
    }

    /**
     * Genera el contenido HTML del email
     */
    private String generarContenidoEmail(NotificacionPago notificacion) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        String tipoNotificacion = notificacion.getTipoNotificacion();
        String colorAlerta = obtenerColorAlerta(tipoNotificacion);
        String iconoAlerta = obtenerIconoAlerta(tipoNotificacion);

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Notificación de Vencimiento - PetManager</title>
            </head>
            <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px;">
                <div style="max-width: 600px; margin: 0 auto; background-color: white; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);">
                    
                    <!-- Header -->
                    <div style="background-color: %s; color: white; padding: 20px; border-radius: 8px 8px 0 0; text-align: center;">
                        <h1 style="margin: 0; font-size: 24px;">%s PetManager</h1>
                        <p style="margin: 10px 0 0 0; font-size: 16px;">Notificación de Condición de Pago</p>
                    </div>
                    
                    <!-- Contenido -->
                    <div style="padding: 30px;">
                        <h2 style="color: #333; margin-top: 0;">Estimado %s,</h2>
                        
                        <p style="font-size: 16px; line-height: 1.6; color: #555;">
                            Le informamos sobre el estado de una condición de pago:
                        </p>
                        
                        <!-- Información de la condición -->
                        <div style="background-color: #f8f9fa; border-left: 4px solid %s; padding: 20px; margin: 20px 0; border-radius: 4px;">
                            <h3 style="margin-top: 0; color: %s;">Detalles de la Condición</h3>
                            <ul style="list-style: none; padding: 0;">
                                <li style="margin: 8px 0;"><strong>📅 Fecha de vencimiento:</strong> %s</li>
                                <li style="margin: 8px 0;"><strong>💳 Días de crédito:</strong> %d días</li>
                                <li style="margin: 8px 0;"><strong>⏰ Días restantes:</strong> %d días</li>
                                <li style="margin: 8px 0;"><strong>📝 Nota:</strong> %s</li>
                            </ul>
                        </div>
                        
                        <!-- Acción requerida -->
                        <div style="background-color: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; border-radius: 4px; margin: 20px 0;">
                            <h4 style="margin-top: 0; color: #856404;">⚡ Acción Requerida</h4>
                            <p style="margin-bottom: 0; color: #856404;">
                                %s
                            </p>
                        </div>
                        
                        <p style="margin-top: 30px; color: #555;">
                            Si tiene alguna consulta, no dude en contactarnos.
                        </p>
                        
                        <p style="color: #555;">
                            Saludos cordiales,<br>
                            <strong>Equipo PetManager</strong>
                        </p>
                    </div>
                    
                    <!-- Footer -->
                    <div style="background-color: #f8f9fa; padding: 20px; border-radius: 0 0 8px 8px; text-align: center; color: #666; font-size: 12px;">
                        <p style="margin: 0;">
                            Este es un email automático del sistema PetManager.<br>
                            Por favor no responder a este mensaje.
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """,
                colorAlerta,                                    // Color header
                iconoAlerta,                                    // Icono header
                notificacion.getNombreProveedor(),              // Nombre proveedor
                colorAlerta,                                    // Color border
                colorAlerta,                                    // Color título
                notificacion.getFechaVencimiento().format(formatter), // Fecha vencimiento
                notificacion.getDiasCredito() != null ? notificacion.getDiasCredito() : 0, // Días crédito
                notificacion.getDiasRestantes() != null ? notificacion.getDiasRestantes() : 0, // Días restantes
                notificacion.getNota() != null ? notificacion.getNota() : "Sin notas adicionales", // Nota
                generarMensajeAccion(notificacion)              // Mensaje de acción
        );
    }

    private String obtenerColorAlerta(String tipoNotificacion) {
        return switch (tipoNotificacion) {
            case "VENCIMIENTO_PROXIMO" -> "#17a2b8";   // Info azul
            case "VENCIMIENTO_INMINENTE" -> "#ffc107"; // Warning amarillo
            case "VENCIMIENTO_HOY" -> "#fd7e14";       // Warning naranja
            case "VENCIDO" -> "#dc3545";               // Danger rojo
            default -> "#6c757d";                      // Secondary gris
        };
    }

    private String obtenerIconoAlerta(String tipoNotificacion) {
        return switch (tipoNotificacion) {
            case "VENCIMIENTO_PROXIMO" -> "🔔";
            case "VENCIMIENTO_INMINENTE" -> "⚠️";
            case "VENCIMIENTO_HOY" -> "🚨";
            case "VENCIDO" -> "❌";
            default -> "📋";
        };
    }

    private String generarMensajeAccion(NotificacionPago notificacion) {
        return switch (notificacion.getTipoNotificacion()) {
            case "VENCIMIENTO_PROXIMO" ->
                    "Recuerde que tiene una condición de pago que vencerá próximamente. " +
                            "Planifique los pagos correspondientes.";
            case "VENCIMIENTO_INMINENTE" ->
                    "Su condición de pago vencerá en pocos días. " +
                            "Asegúrese de realizar los pagos pendientes antes del vencimiento.";
            case "VENCIMIENTO_HOY" ->
                    "Su condición de pago vence HOY. " +
                            "Realice los pagos pendientes para evitar inconvenientes.";
            case "VENCIDO" ->
                    "Su condición de pago ha VENCIDO. " +
                            "Contacte inmediatamente para regularizar la situación.";
            default ->
                    "Revise los detalles de su condición de pago y tome las acciones necesarias.";
        };
    }

    /**
     * Verifica si el servicio de email está disponible
     */
    public boolean verificarServicioEmail() {
        log.info("🔍 Verificando servicio de email...");
        // Por ahora siempre retorna true
        // Después implementaremos verificación real con Brevo
        return true;
    }
}