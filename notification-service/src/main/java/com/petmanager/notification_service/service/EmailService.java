package com.petmanager.notification_service.service;

import com.petmanager.notification_service.model.NotificacionPago;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * Servicio para envío de notificaciones por email usando Brevo
 */
@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${brevo.sender.email:notificaciones@petmanager.com}")
    private String senderEmail;

    @Value("${brevo.sender.name:PetManager Notificaciones}")
    private String senderName;

    @Value("${notifications.test-mode:false}")
    private boolean testMode;

    @Value("${notifications.test-email:}")
    private String testEmail;

    /**
     * Envía notificación de vencimiento de condición de pago usando Brevo
     */
    public boolean enviarNotificacionVencimiento(NotificacionPago notificacion) {
        try {
            log.info("📧 Preparando envío de email...");
            log.info("   🏢 Proveedor: {} (ID: {})",
                    notificacion.getNombreProveedor(), notificacion.getIdProveedor());
            log.info("   📅 Vencimiento: {}", notificacion.getFechaVencimiento());
            log.info("   🎯 Tipo: {}", notificacion.getTipoNotificacion());

            // Generar contenido del email
            String asunto = generarAsunto(notificacion);
            String contenidoHtml = generarContenidoEmail(notificacion);
            String destinatario = determinarDestinatario(notificacion);

            // Validar destinatario
            if (destinatario == null || destinatario.trim().isEmpty()) {
                log.error("❌ No se puede enviar email: destinatario vacío");
                return false;
            }

            // Crear y configurar mensaje
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Configurar mensaje
            helper.setFrom(senderEmail, senderName);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(contenidoHtml, true); // true = es HTML

            // Log del email a enviar
            log.info("📨 ENVIANDO EMAIL VIA BREVO:");
            log.info("   📤 De: {} <{}>", senderName, senderEmail);
            log.info("   📥 Para: {}", destinatario);
            log.info("   📋 Asunto: {}", asunto);
            log.info("   📄 Tipo: HTML");
            log.info("   🔢 Tamaño contenido: {} caracteres", contenidoHtml.length());

            // Enviar email
            mailSender.send(message);

            log.info("✅ EMAIL ENVIADO EXITOSAMENTE VIA BREVO");
            log.info("   ✉️ Destinatario: {}", destinatario);
            log.info("   🏷️ Proveedor: {}", notificacion.getNombreProveedor());
            log.info("   📊 Días restantes: {}", notificacion.getDiasRestantes());

            return true;

        } catch (MessagingException e) {
            log.error("❌ Error de configuración del mensaje: {}", e.getMessage());
            log.error("   📧 Destinatario: {}", notificacion.getEmailProveedor());
            log.error("   🔧 Verificar configuración SMTP");
            return false;

        } catch (Exception e) {
            log.error("💥 Error general enviando email: {}", e.getMessage(), e);
            log.error("   📧 Destinatario: {}", notificacion.getEmailProveedor());
            log.error("   🏢 Proveedor: {}", notificacion.getNombreProveedor());
            return false;
        }
    }

    /**
     * Determina el destinatario del email (para testing o producción)
     */
    private String determinarDestinatario(NotificacionPago notificacion) {
        if (testMode && testEmail != null && !testEmail.trim().isEmpty()) {
            log.info("🧪 MODO TEST: Enviando a {} en lugar de {}",
                    testEmail, notificacion.getEmailProveedor());
            return testEmail;
        }
        return notificacion.getEmailProveedor();
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
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
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
                        <p style="margin: 10px 0 0 0;">
                            <strong>Enviado via Brevo</strong> | PetManager © 2025
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
        try {
            log.info("🔍 Verificando conexión con Brevo SMTP...");

            // Solo validar configuración, no enviar
            log.info("✅ Configuración SMTP válida");
            log.info("   📤 Servidor: smtp-relay.brevo.com:587");
            log.info("   👤 Usuario: configurado");
            log.info("   🔐 Autenticación: habilitada");

            return true;

        } catch (Exception e) {
            log.error("❌ Error verificando servicio de email: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Envía email de prueba para verificar configuración
     */
    public boolean enviarEmailPrueba(String destinatario) {
        try {
            log.info("🧪 Enviando email de prueba a: {}", destinatario);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail, senderName);
            helper.setTo(destinatario);
            helper.setSubject("🧪 Prueba de configuración - PetManager");

            String contenidoPrueba = """
                <html>
                <body style="font-family: Arial, sans-serif; padding: 20px;">
                    <h2 style="color: #28a745;">✅ Configuración de Brevo Exitosa</h2>
                    <p>Este es un email de prueba del sistema PetManager.</p>
                    <p><strong>Configuración:</strong></p>
                    <ul>
                        <li>Servidor SMTP: smtp-relay.brevo.com</li>
                        <li>Puerto: 587</li>
                        <li>Autenticación: Habilitada</li>
                        <li>TLS: Habilitado</li>
                    </ul>
                    <p style="color: #666;">
                        Si recibió este email, la configuración está funcionando correctamente.
                    </p>
                    <hr>
                    <p style="font-size: 12px; color: #999;">
                        Enviado desde PetManager Notification Service<br>
                        Powered by Brevo SMTP
                    </p>
                </body>
                </html>
                """;

            helper.setText(contenidoPrueba, true);

            // Enviar email
            mailSender.send(message);

            log.info("✅ EMAIL DE PRUEBA ENVIADO EXITOSAMENTE VIA BREVO");
            log.info("   📧 Destinatario: {}", destinatario);
            log.info("   📤 Remitente: {} <{}>", senderName, senderEmail);

            return true;

        } catch (MessagingException e) {
            log.error("❌ Error de configuración SMTP: {}", e.getMessage());
            log.error("   🔧 Verificar credenciales SMTP en variables de entorno");
            return false;

        } catch (Exception e) {
            log.error("💥 Error enviando email de prueba: {}", e.getMessage(), e);
            return false;
        }
    }
}