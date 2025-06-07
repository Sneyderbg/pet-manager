package com.petmanager.auth_service.controller;

import com.petmanager.auth_service.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/test-email")
@Slf4j
public class EmailTestController {

    @Autowired
    private EmailService emailService;

    /**
     * Endpoint original para emails simples
     */
    @PostMapping("/simple")
    public ResponseEntity<String> enviarCorreoSimple(@RequestParam String to,
                                                     @RequestParam String subject,
                                                     @RequestParam String message) {
        log.info("📧 Test email simple solicitado para: {}", to);

        try {
            emailService.enviarCorreo(to, subject, message);
            return ResponseEntity.ok("✅ Correo simple enviado a " + to);
        } catch (Exception e) {
            log.error("❌ Error enviando correo simple: {}", e.getMessage());
            return ResponseEntity.badRequest().body("❌ Error: " + e.getMessage());
        }
    }

    /**
     * NUEVO: Endpoint para probar la plantilla HTML elegante de recuperación
     */
    @PostMapping("/recuperacion")
    public ResponseEntity<String> probarPlantillaRecuperacion(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String nombreUsuario = request.getOrDefault("nombre", "Usuario de Prueba");

        log.info("🎨 Test de plantilla de recuperación solicitado para: {}", email);

        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("❌ Email es requerido");
        }

        try {
            // Generar token de prueba
            String tokenPrueba = UUID.randomUUID().toString();

            // Enviar email con plantilla elegante
            boolean enviado = emailService.enviarCorreoRecuperacion(email, tokenPrueba, nombreUsuario);

            if (enviado) {
                log.info("✅ Plantilla de recuperación enviada exitosamente");
                return ResponseEntity.ok(String.format("""
                    ✅ Email de recuperación enviado exitosamente
                    📧 Destinatario: %s
                    👤 Usuario: %s
                    🔑 Token de prueba: %s...
                    🎨 Plantilla: HTML elegante con gradientes
                    """, email, nombreUsuario, tokenPrueba.substring(0, 8)));
            } else {
                return ResponseEntity.badRequest().body("❌ Error enviando email de recuperación");
            }

        } catch (Exception e) {
            log.error("❌ Error en test de plantilla: {}", e.getMessage());
            return ResponseEntity.badRequest().body("❌ Error: " + e.getMessage());
        }
    }

    /**
     * NUEVO: Endpoint para probar configuración básica de email
     */
    @PostMapping("/config")
    public ResponseEntity<String> probarConfiguracion(@RequestParam String email) {
        log.info("🔧 Test de configuración solicitado para: {}", email);

        try {
            boolean enviado = emailService.enviarEmailPrueba(email);

            if (enviado) {
                return ResponseEntity.ok(String.format("""
                    ✅ Test de configuración exitoso
                    📧 Email enviado a: %s
                    🔧 SMTP: Funcionando correctamente
                    🎯 Brevo: Conectado
                    """, email));
            } else {
                return ResponseEntity.badRequest().body("❌ Error en configuración SMTP");
            }

        } catch (Exception e) {
            log.error("❌ Error en test de configuración: {}", e.getMessage());
            return ResponseEntity.badRequest().body("❌ Error de configuración: " + e.getMessage());
        }
    }

    /**
     * Health check del servicio de email
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        try {
            boolean disponible = emailService.verificarServicioEmail();

            if (disponible) {
                return ResponseEntity.ok("""
                    ✅ Email Service Health Check
                    🟢 Estado: Funcionando
                    📨 SMTP: Configurado
                    🎨 Plantillas: Disponibles
                    """);
            } else {
                return ResponseEntity.badRequest().body("❌ Servicio de email no disponible");
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("❌ Error en health check: " + e.getMessage());
        }
    }
}