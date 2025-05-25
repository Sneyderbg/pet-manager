package com.petmanager.auth_service.controller;

import com.petmanager.auth_service.service.PasswordResetService;
import com.petmanager.auth_service.repository.UserRepository;
import com.petmanager.auth_service.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

import java.util.Optional;

@RestController
@RequestMapping("/password")
public class PasswordResetController {

    @Autowired
    private PasswordResetService resetService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    //  Endpoint para solicitar el token de recuperación
    @PostMapping("/request-reset")
    public ResponseEntity<?> requestReset(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        resetService.createPasswordResetToken(email);
        return ResponseEntity.ok("Se envió un correo con el enlace para restablecer la contraseña.");
    }

    //  Endpoint para restablecer la contraseña con el token
    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");

        Long userId = resetService.validateToken(token);

        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest().body("Usuario no encontrado.");
        }

        User user = optionalUser.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetService.invalidateToken(token);

        return ResponseEntity.ok("Contraseña actualizada correctamente.");
    }
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        try {
            resetService.validateToken(token);  // si no lanza excepción, es válido
            return ResponseEntity.ok("Token válido.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
