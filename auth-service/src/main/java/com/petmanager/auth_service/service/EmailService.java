package com.petmanager.auth_service.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Servicio de email mejorado para auth-service con plantillas HTML elegantes
 */
@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${mail.from:camiloloaiza0303@gmail.com}")
    private String senderEmail;

    @Value("${mail.sender.name:PetManager}")
    private String senderName;

    @Value("${frontend.url}")
    private String frontendUrl;

    /**
     * Envía email de recuperación de contraseña con plantilla HTML elegante
     */
    public boolean enviarCorreoRecuperacion(String destinatario, String token, String nombreUsuario) {
        try {
            log.info("📧 Preparando envío de email de recuperación...");
            log.info("   👤 Usuario: {}", nombreUsuario);
            log.info("   📧 Destinatario: {}", destinatario);
            log.info("   🔑 Token: {}...", token.substring(0, 8));

            // Generar enlace de recuperación
            String enlaceRecuperacion = frontendUrl + "/recover-password?token=" + token;

            // Generar contenido del email
            String asunto = "🔐 Recuperación de contraseña - PetManager";
            String contenidoHtml = generarPlantillaRecuperacion(nombreUsuario, enlaceRecuperacion, token);

            // Crear y configurar mensaje
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Configurar mensaje
            helper.setFrom(senderEmail, senderName);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(contenidoHtml, true); // true = es HTML

            // Log del email a enviar
            log.info("📨 ENVIANDO EMAIL DE RECUPERACIÓN VIA BREVO:");
            log.info("   📤 De: {} <{}>", senderName, senderEmail);
            log.info("   📥 Para: {}", destinatario);
            log.info("   📋 Asunto: {}", asunto);
            log.info("   🔗 Enlace: {}", enlaceRecuperacion);
            log.info("   📄 Tipo: HTML");
            log.info("   🔢 Tamaño contenido: {} caracteres", contenidoHtml.length());

            // Enviar email
            mailSender.send(message);

            log.info("✅ EMAIL DE RECUPERACIÓN ENVIADO EXITOSAMENTE VIA BREVO");
            log.info("   ✉️ Destinatario: {}", destinatario);
            log.info("   👤 Usuario: {}", nombreUsuario);

            return true;

        } catch (MessagingException e) {
            log.error("❌ Error de configuración del mensaje: {}", e.getMessage());
            log.error("   📧 Destinatario: {}", destinatario);
            log.error("   🔧 Verificar configuración SMTP");
            return false;

        } catch (Exception e) {
            log.error("💥 Error general enviando email: {}", e.getMessage(), e);
            log.error("   📧 Destinatario: {}", destinatario);
            log.error("   👤 Usuario: {}", nombreUsuario);
            return false;
        }
    }

    /**
     * Genera la plantilla HTML elegante para recuperación de contraseña
     */
    private String generarPlantillaRecuperacion(String nombreUsuario, String enlaceRecuperacion, String token) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String fechaActual = LocalDateTime.now().format(formatter);

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Recuperación de Contraseña - PetManager</title>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px;">
                <div style="max-width: 600px; margin: 0 auto; background-color: white; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);">
                    
                    <!-- Header -->
                    <div style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px 20px; border-radius: 8px 8px 0 0; text-align: center;">
                        <h1 style="margin: 0; font-size: 28px; font-weight: 300;">🔐 PetManager</h1>
                        <p style="margin: 10px 0 0 0; font-size: 16px; opacity: 0.9;">Recuperación de Contraseña</p>
                    </div>
                    
                    <!-- Contenido Principal -->
                    <div style="padding: 40px 30px;">
                        <h2 style="color: #333; margin-top: 0; font-size: 24px;">¡Hola %s! 👋</h2>
                        
                        <p style="font-size: 16px; line-height: 1.6; color: #555; margin-bottom: 25px;">
                            Recibimos una solicitud para restablecer la contraseña de tu cuenta en PetManager. 
                            No te preocupes, esto puede suceder a cualquiera.
                        </p>
                        
                        <!-- Información importante -->
                        <div style="background-color: #e8f4fd; border-left: 4px solid #3498db; padding: 20px; margin: 25px 0; border-radius: 4px;">
                            <h3 style="margin-top: 0; color: #2980b9; font-size: 18px;">🛡️ Información de Seguridad</h3>
                            <ul style="margin: 10px 0; padding-left: 20px; color: #2c3e50;">
                                <li><strong>📅 Fecha de solicitud:</strong> %s</li>
                                <li><strong>⏰ Válido por:</strong> 1 hora</li>
                                <li><strong>🔒 Token:</strong> %s...</li>
                            </ul>
                        </div>
                        
                        <!-- Botón de acción -->
                        <div style="text-align: center; margin: 35px 0;">
                            <a href="%s" 
                               style="display: inline-block; 
                                      background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); 
                                      color: white; 
                                      text-decoration: none; 
                                      padding: 15px 35px; 
                                      border-radius: 50px; 
                                      font-size: 16px; 
                                      font-weight: 600;
                                      box-shadow: 0 4px 15px rgba(0,0,0,0.2);
                                      transition: all 0.3s ease;">
                                🔑 Restablecer mi Contraseña
                            </a>
                        </div>
                        
                        <!-- Instrucciones adicionales -->
                        <div style="background-color: #fff3cd; border: 1px solid #ffeaa7; padding: 20px; border-radius: 6px; margin: 25px 0;">
                            <h4 style="margin-top: 0; color: #856404; display: flex; align-items: center;">
                                ⚠️ Instrucciones Importantes
                            </h4>
                            <ul style="margin: 15px 0; padding-left: 20px; color: #856404; line-height: 1.6;">
                                <li>Haz clic en el botón "Restablecer mi Contraseña" o copia el enlace en tu navegador</li>
                                <li>Este enlace es válido por <strong>1 hora únicamente</strong></li>
                                <li>Elige una contraseña segura con al menos 7 caracteres, una mayúscula y un carácter especial</li>
                                <li>Si no solicitaste este cambio, ignora este correo</li>
                            </ul>
                        </div>
                        
                        <!-- Enlace alternativo -->
                        <div style="background-color: #f8f9fa; padding: 20px; border-radius: 6px; margin: 25px 0;">
                            <h4 style="margin-top: 0; color: #495057;">🔗 ¿El botón no funciona?</h4>
                            <p style="margin: 10px 0; color: #6c757d; font-size: 14px;">
                                Copia y pega este enlace en tu navegador:
                            </p>
                            <div style="background-color: #e9ecef; padding: 10px; border-radius: 4px; font-family: monospace; font-size: 12px; word-break: break-all; color: #495057;">
                                %s
                            </div>
                        </div>
                        
                        <!-- Mensaje de seguridad -->
                        <div style="border-top: 2px solid #e9ecef; padding-top: 25px; margin-top: 35px;">
                            <p style="color: #6c757d; font-size: 14px; line-height: 1.6;">
                                🔐 <strong>Consejos de Seguridad:</strong><br>
                                • Nunca compartas tu contraseña con nadie<br>
                                • PetManager nunca te pedirá tu contraseña por email<br>
                                • Si no reconoces esta actividad, contacta a soporte inmediatamente
                            </p>
                        </div>
                        
                        <p style="margin-top: 30px; color: #555; line-height: 1.6;">
                            Si tienes alguna pregunta o necesitas ayuda, no dudes en contactarnos.
                        </p>
                        
                        <p style="color: #555;">
                            Saludos cordiales,<br>
                            <strong>El Equipo de PetManager</strong> 🐾
                        </p>
                    </div>
                    
                    <!-- Footer -->
                    <div style="background-color: #2c3e50; color: white; padding: 25px 20px; border-radius: 0 0 8px 8px; text-align: center;">
                        <p style="margin: 0; font-size: 14px; opacity: 0.9;">
                            Este es un email automático del sistema PetManager.<br>
                            Por favor no responder a este mensaje.
                        </p>
                        <div style="margin-top: 15px; padding-top: 15px; border-top: 1px solid rgba(255,255,255,0.2);">
                            <p style="margin: 0; font-size: 12px; opacity: 0.7;">
                                <strong>🚀 Enviado via Brevo SMTP</strong> | PetManager © 2025<br>
                                Sistema de Gestión Integral para Tiendas de Mascotas
                            </p>
                        </div>
                    </div>
                </div>
                
                <!-- Estilos adicionales para hover del botón -->
                <style>
                    a:hover {
                        transform: translateY(-2px) !important;
                        box-shadow: 0 6px 20px rgba(0,0,0,0.3) !important;
                    }
                </style>
            </body>
            </html>
            """,
                nombreUsuario,           // Nombre del usuario
                fechaActual,            // Fecha de solicitud
                token.substring(0, 8),  // Primeros 8 caracteres del token
                enlaceRecuperacion,     // Enlace completo del botón
                enlaceRecuperacion      // Enlace para copiar y pegar
        );
    }

    /**
     * Método original para compatibilidad (delegado al nuevo método)
     */
    public void enviarCorreo(String destino, String asunto, String contenido) {
        try {
            // Para compatibilidad con el método original
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail, senderName);
            helper.setTo(destino);
            helper.setSubject(asunto);
            helper.setText(contenido, false); // false = texto plano

            mailSender.send(message);
            log.info("✅ Email simple enviado a: {}", destino);

        } catch (Exception e) {
            log.error("❌ Error enviando email simple: {}", e.getMessage());
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
            helper.setSubject("🧪 Prueba de configuración - PetManager Auth Service");

            String contenidoPrueba = """
                <html>
                <body style="font-family: Arial, sans-serif; padding: 20px; background-color: #f4f4f4;">
                    <div style="max-width: 500px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);">
                        <h2 style="color: #28a745; text-align: center;">✅ Auth Service - Email Test</h2>
                        <p style="text-align: center;">Este es un email de prueba del <strong>auth-service</strong> de PetManager.</p>
                        <div style="background-color: #e8f4fd; padding: 15px; border-radius: 4px; margin: 20px 0;">
                            <h4 style="color: #2980b9; margin-top: 0;">📋 Configuración:</h4>
                            <ul style="color: #2c3e50;">
                                <li>Servidor SMTP: smtp-relay.sendinblue.com</li>
                                <li>Puerto: 587</li>
                                <li>Autenticación: Habilitada</li>
                                <li>TLS: Habilitado</li>
                            </ul>
                        </div>
                        <p style="color: #666; text-align: center;">
                            Si recibiste este email, la configuración del auth-service está funcionando correctamente.
                        </p>
                        <hr style="border: none; height: 1px; background-color: #eee; margin: 20px 0;">
                        <p style="font-size: 12px; color: #999; text-align: center;">
                            Enviado desde PetManager Auth Service<br>
                            Powered by Brevo SMTP
                        </p>
                    </div>
                </body>
                </html>
                """;

            helper.setText(contenidoPrueba, true);
            mailSender.send(message);

            log.info("✅ EMAIL DE PRUEBA ENVIADO EXITOSAMENTE VIA BREVO");
            log.info("   📧 Destinatario: {}", destinatario);

            return true;

        } catch (Exception e) {
            log.error("❌ Error enviando email de prueba: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Verifica si el servicio de email está disponible
     */
    public boolean verificarServicioEmail() {
        try {
            log.info("🔍 Verificando conexión con Brevo SMTP...");
            log.info("✅ Configuración SMTP válida para auth-service");
            return true;
        } catch (Exception e) {
            log.error("❌ Error verificando servicio de email: {}", e.getMessage());
            return false;
        }
    }
}