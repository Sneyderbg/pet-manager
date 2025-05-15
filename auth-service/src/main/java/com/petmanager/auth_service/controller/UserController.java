package com.petmanager.auth_service.controller;

import java.util.ArrayList;
import com.petmanager.auth_service.dto.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import com.petmanager.auth_service.dto.UserRequest;
import com.petmanager.auth_service.service.UserService;
import com.petmanager.auth_service.service.JwtService;
import com.petmanager.auth_service.model.User;
import com.petmanager.auth_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    // Buscar un usuario por email (ejemplo: /api/users/email?email=test@email.com)
    @GetMapping("/email")
    public ResponseEntity<User> getUserByEmail(@RequestParam String email) {
        System.out.println("Email recibido: " + email);
        return userRepository.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Nuevo endpoint para registrar usuarios
    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody UserRequest userRequest) {
        User newUser = userService.registerUser(userRequest);
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hola, estás autenticado como ADMIN con JWT ✔");
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7); // Quitar "Bearer "
        String email = jwtService.extractUsername(token);

        return userRepository.findByEmail(email)
                .map(user -> {
                    UserResponse dto = new UserResponse(
                            user.getId(),
                            user.getNombre(),
                            user.getEmail(),
                            user.getActivo(),
                            new ArrayList<>(user.getRoles()) // Conversión de Set a List
                    );
                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
    }

}
