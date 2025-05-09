package com.petmanager.auth_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import com.petmanager.auth_service.dto.UserRequest;
import com.petmanager.auth_service.service.UserService;
import com.petmanager.auth_service.model.User;
import com.petmanager.auth_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
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
}


