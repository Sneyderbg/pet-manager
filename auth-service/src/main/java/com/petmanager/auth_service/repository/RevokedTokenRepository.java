package com.petmanager.auth_service.repository;

import com.petmanager.auth_service.model.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {
    Optional<RevokedToken> findByToken(String token);
    boolean existsByToken(String token);

    // Nuevo método para limpiar tokens antiguos
    void deleteAllByRevokedAtBefore(LocalDateTime time);
}
