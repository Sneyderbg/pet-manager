package com.petmanager.auth_service.service;

import com.petmanager.auth_service.dto.UserRequest;
import com.petmanager.auth_service.model.User;
import com.petmanager.auth_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // Registro con DTO (REST)
    public User registerUser(UserRequest userRequest) {
        if (userRepository.findByEmail(userRequest.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario registrado con ese correo.");
        }

        validatePasswordOrThrow(userRequest.getPassword());

        User user = new User();
        user.setNombre(userRequest.getNombre());
        user.setEmail(userRequest.getEmail());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setFechaCreacion(LocalDateTime.now());
        user.setActivo(true);

        return userRepository.save(user);
    }

    // Registro directo (GraphQL)
    public User registerUser(String nombre, String email, String password) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario registrado con ese correo.");
        }

        validatePasswordOrThrow(password);

        User user = new User();
        user.setNombre(nombre);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFechaCreacion(LocalDateTime.now());
        user.setActivo(true);

        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Validación explícita separada
    private boolean isValidPassword(String password) {
        return password.length() >= 7 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[^a-zA-Z0-9].*");
    }

    private void validatePasswordOrThrow(String password) {
        if (!isValidPassword(password)) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 7 caracteres, una letra mayúscula y un carácter especial.");
        }
    }
}


