package com.petmanager.auth_service.controller;

import com.petmanager.auth_service.service.PasswordResetService;
import com.petmanager.auth_service.repository.UserRepository;
import com.petmanager.auth_service.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

import java.util.Optional;

@RestController
@RequestMapping("/password")
@Slf4j
public class PasswordResetController {

    @Autowired
    private PasswordResetService resetService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Endpoint para solicitar el token de recuperación con plantilla HTML elegante
     */
    @PostMapping("/request-reset")
    public ResponseEntity<?> requestReset(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        log.info("🔐 Solicitud de recuperación de contraseña recibida");
        log.info("   📧 Email: {}", email);

        if (email == null || email.trim().isEmpty()) {
            log.warn("⚠️ Solicitud sin email");
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "El email es requerido"
            ));
        }

        try {
            resetService.createPasswordResetToken(email.trim().toLowerCase());

            log.info("✅ Proceso de recuperación iniciado exitosamente para: {}", email);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Se envió un correo con el enlace para restablecer la contraseña.",
                    "details", "Revisa tu bandeja de entrada y spam. El enlace es válido por 1 hora."
            ));

        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Error de validación en recuperación: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));

        } catch (Exception e) {
            log.error("💥 Error interno en recuperación: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error interno del servidor. Intente nuevamente."
            ));
        }
    }

    /**
     * Endpoint para restablecer la contraseña con el token
     */
    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");

        log.info("🔄 Solicitud de restablecimiento de contraseña recibida");
        log.info("   🔑 Token: {}...", token != null ? token.substring(0, 8) : "null");

        // Validaciones de entrada
        if (token == null || token.trim().isEmpty()) {
            log.warn("⚠️ Token vacío en restablecimiento");
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Token es requerido"
            ));
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            log.warn("⚠️ Contraseña vacía en restablecimiento");
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "La nueva contraseña es requerida"
            ));
        }

        // Validar fortaleza de contraseña
        if (!isValidPassword(newPassword)) {
            log.warn("⚠️ Contraseña no cumple criterios de seguridad");
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "La contraseña debe tener al menos 7 caracteres, una letra mayúscula y un carácter especial."
            ));
        }

        try {
            // Validar token y obtener usuario
            Long userId = resetService.validateToken(token.trim());

            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isEmpty()) {
                log.error("❌ Usuario no encontrado con ID: {}", userId);
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Usuario no encontrado."
                ));
            }

            User user = optionalUser.get();
            log.info("👤 Restableciendo contraseña para usuario: {} (ID: {})", user.getNombre(), user.getId());

            // Verificar que el usuario esté activo
            if (!user.isActivo()) {
                log.warn("⚠️ Intento de restablecimiento para usuario inactivo: {}", user.getEmail());
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "La cuenta está desactivada. Contacte al administrador."
                ));
            }

            // Actualizar contraseña
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            // Invalidar token
            resetService.invalidateToken(token.trim());

            log.info("✅ Contraseña restablecida exitosamente para usuario: {}", user.getEmail());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Contraseña actualizada correctamente.",
                    "details", "Ya puedes iniciar sesión con tu nueva contraseña."
            ));

        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Error de validación en restablecimiento: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));

        } catch (Exception e) {
            log.error("💥 Error interno en restablecimiento: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error interno del servidor. Intente nuevamente."
            ));
        }
    }

    /**
     * Endpoint para validar token (útil para el frontend)
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        log.info("🔍 Validación de token solicitada: {}...", token.substring(0, 8));

        if (token == null || token.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "valid", false,
                    "message", "Token es requerido"
            ));
        }

        try {
            Long userId = resetService.validateToken(token.trim());

            // Obtener información básica del usuario
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();

                log.info("✅ Token válido para usuario: {}", user.getEmail());

                return ResponseEntity.ok(Map.of(
                        "valid", true,
                        "message", "Token válido",
                        "userEmail", user.getEmail(),
                        "userName", user.getNombre()
                ));
            } else {
                log.warn("⚠️ Token válido pero usuario no encontrado");
                return ResponseEntity.badRequest().body(Map.of(
                        "valid", false,
                        "message", "Usuario no encontrado"
                ));
            }

        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Token inválido: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "valid", false,
                    "message", e.getMessage()
            ));

        } catch (Exception e) {
            log.error("💥 Error validando token: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "valid", false,
                    "message", "Error interno del servidor"
            ));
        }
    }

    /**
     * Valida que la contraseña cumple los criterios de seguridad
     */
    private boolean isValidPassword(String password) {
        return password.length() >= 7 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[^a-zA-Z0-9].*");
    }
}
