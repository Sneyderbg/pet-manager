package com.petmanager.auth_service.controller;

import com.petmanager.auth_service.service.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class LogoutController {

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenBlacklistService.revokeToken(token);
            return ResponseEntity.ok("Sesión cerrada correctamente.");
        }

        return ResponseEntity.badRequest().body("No se proporcionó un token válido.");
    }
}
