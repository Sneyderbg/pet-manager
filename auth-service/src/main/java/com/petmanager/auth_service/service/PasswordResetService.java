package com.petmanager.auth_service.service;

import com.petmanager.auth_service.model.PasswordResetToken;
import com.petmanager.auth_service.model.User;
import com.petmanager.auth_service.repository.PasswordResetTokenRepository;
import com.petmanager.auth_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;


import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Value("${frontend.url}")
    private String frontendUrl;


    public void createPasswordResetToken(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("No existe un usuario con ese correo.");
        }

        User user = optionalUser.get();

        // Eliminar tokens anteriores
        tokenRepository.deleteByUserId(user.getId());

        // Crear uno nuevo
        String token = UUID.randomUUID().toString();
        LocalDateTime expiration = LocalDateTime.now().plusHours(1);

        PasswordResetToken resetToken = new PasswordResetToken(token, user.getId(), expiration);
        tokenRepository.save(resetToken);

        String link = frontendUrl + "/recover-password?token=" + token;
        String cuerpo = "Haz clic en el siguiente enlace para cambiar tu contraseña:\n" + link;

        emailService.enviarCorreo(email, "Restablece tu contraseña", cuerpo);
    }

    public Long validateToken(String token) {
        Optional<PasswordResetToken> optional = tokenRepository.findByToken(token);

        if (optional.isEmpty()) {
            throw new IllegalArgumentException("Token inválido.");
        }

        PasswordResetToken resetToken = optional.get();

        if (resetToken.getExpiration().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(resetToken);
            throw new IllegalArgumentException("El token ha expirado.");
        }

        return resetToken.getUserId();
    }

    public void invalidateToken(String token) {
        tokenRepository.findByToken(token).ifPresent(tokenRepository::delete);
    }
}
