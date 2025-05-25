package com.petmanager.auth_service.service;

import com.petmanager.auth_service.model.RevokedToken;
import com.petmanager.auth_service.repository.RevokedTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TokenBlacklistService {

    @Autowired
    private RevokedTokenRepository revokedTokenRepository;

    // Guarda el token revocado
    public void revokeToken(String token) {
        if (!revokedTokenRepository.existsByToken(token)) {
            RevokedToken revoked = new RevokedToken(token, LocalDateTime.now());
            revokedTokenRepository.save(revoked);
        }

        // Limpieza de tokens antiguos (opcional pero recomendado)
        cleanOldRevokedTokens();
    }

    // Verifica si un token está revocado
    public boolean isTokenRevoked(String token) {
        return revokedTokenRepository.existsByToken(token);
    }

    // Elimina tokens revocados hace más de 24 horas
    private void cleanOldRevokedTokens() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);
        revokedTokenRepository.deleteAllByRevokedAtBefore(threshold);
    }
}

