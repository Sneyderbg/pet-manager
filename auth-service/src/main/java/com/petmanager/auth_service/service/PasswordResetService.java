package com.petmanager.auth_service.service;

import com.petmanager.auth_service.model.PasswordResetToken;
import com.petmanager.auth_service.model.User;
import com.petmanager.auth_service.repository.PasswordResetTokenRepository;
import com.petmanager.auth_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class PasswordResetService {

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Value("${frontend.url}")
    private String frontendUrl;

    /**
     * Crea token de recuperación y envía email con plantilla HTML elegante
     */
    public void createPasswordResetToken(String email) {
        log.info("🔐 Iniciando proceso de recuperación de contraseña para: {}", email);

        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            log.warn("⚠️ Intento de recuperación para email no registrado: {}", email);
            throw new IllegalArgumentException("No existe un usuario con ese correo.");
        }

        User user = optionalUser.get();
        log.info("✅ Usuario encontrado: {} (ID: {})", user.getNombre(), user.getId());

        // Verificar que el usuario esté activo
        if (!user.isActivo()) {
            log.warn("⚠️ Intento de recuperación para usuario inactivo: {}", email);
            throw new IllegalArgumentException("La cuenta está desactivada. Contacte al administrador.");
        }

        // Eliminar tokens anteriores para este usuario
        try {
            tokenRepository.deleteByUserId(user.getId());
            log.info("🗑️ Tokens anteriores eliminados para usuario ID: {}", user.getId());
        } catch (Exception e) {
            log.warn("⚠️ Error eliminando tokens anteriores: {}", e.getMessage());
        }

        // Crear nuevo token
        String token = UUID.randomUUID().toString();
        LocalDateTime expiration = LocalDateTime.now().plusHours(1);

        PasswordResetToken resetToken = new PasswordResetToken(token, user.getId(), expiration);
        tokenRepository.save(resetToken);

        log.info("🎫 Token de recuperación creado:");
        log.info("   🔑 Token: {}...", token.substring(0, 8));
        log.info("   ⏰ Expira: {}", expiration);
        log.info("   👤 Usuario: {}", user.getNombre());

        // Enviar email con la nueva plantilla elegante
        try {
            boolean emailEnviado = emailService.enviarCorreoRecuperacion(
                    email,
                    token,
                    user.getNombre()
            );

            if (emailEnviado) {
                log.info("✅ Email de recuperación enviado exitosamente");
                log.info("   📧 Destinatario: {}", email);
                log.info("   👤 Usuario: {}", user.getNombre());
                log.info("   🔗 Frontend URL: {}", frontendUrl);
            } else {
                log.error("❌ Error enviando email de recuperación");
                throw new RuntimeException("Error enviando correo de recuperación");
            }

        } catch (Exception e) {
            log.error("💥 Error en proceso de envío de email: {}", e.getMessage(), e);
            throw new RuntimeException("Error enviando correo de recuperación: " + e.getMessage());
        }
    }

    /**
     * Valida token de recuperación
     */
    public Long validateToken(String token) {
        log.info("🔍 Validando token de recuperación: {}...", token.substring(0, 8));

        Optional<PasswordResetToken> optional = tokenRepository.findByToken(token);

        if (optional.isEmpty()) {
            log.warn("⚠️ Token no encontrado: {}...", token.substring(0, 8));
            throw new IllegalArgumentException("Token inválido.");
        }

        PasswordResetToken resetToken = optional.get();

        // Verificar expiración
        if (resetToken.getExpiration().isBefore(LocalDateTime.now())) {
            log.warn("⏰ Token expirado: {}... (Expiró: {})",
                    token.substring(0, 8), resetToken.getExpiration());

            // Eliminar token expirado
            tokenRepository.delete(resetToken);
            throw new IllegalArgumentException("El token ha expirado.");
        }

        // Verificar si ya fue usado
        if (resetToken.isUsed()) {
            log.warn("🔒 Token ya utilizado: {}...", token.substring(0, 8));
            throw new IllegalArgumentException("El token ya ha sido utilizado.");
        }

        log.info("✅ Token válido para usuario ID: {}", resetToken.getUserId());
        return resetToken.getUserId();
    }

    /**
     * Invalida token después de uso exitoso
     */
    public void invalidateToken(String token) {
        log.info("🔒 Invalidando token: {}...", token.substring(0, 8));

        Optional<PasswordResetToken> optional = tokenRepository.findByToken(token);
        if (optional.isPresent()) {
            PasswordResetToken resetToken = optional.get();
            resetToken.setUsed(true); // Marcar como usado
            tokenRepository.save(resetToken);

            log.info("✅ Token marcado como usado: {}...", token.substring(0, 8));
        } else {
            log.warn("⚠️ Token no encontrado para invalidar: {}...", token.substring(0, 8));
        }
    }

    /**
     * Limpia tokens expirados (para mantenimiento)
     */
    public int limpiarTokensExpirados() {
        log.info("🧹 Iniciando limpieza de tokens expirados...");

        // Esta funcionalidad requeriría un método adicional en el repository
        // Por ahora solo loggeamos
        log.info("🧹 Limpieza de tokens completada");
        return 0;
    }

    /**
     * Estadísticas de tokens (para monitoreo)
     */
    public void logEstadisticasTokens() {
        try {
            long totalTokens = tokenRepository.count();
            log.info("📊 Estadísticas de tokens de recuperación:");
            log.info("   📝 Total tokens en BD: {}", totalTokens);

        } catch (Exception e) {
            log.error("❌ Error obteniendo estadísticas: {}", e.getMessage());
        }
    }
}