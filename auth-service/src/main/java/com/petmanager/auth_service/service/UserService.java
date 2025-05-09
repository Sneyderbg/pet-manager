package com.petmanager.auth_service.service;

import com.petmanager.auth_service.dto.UserRequest;
import com.petmanager.auth_service.model.User;
import com.petmanager.auth_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Método original
    public User registerUser(UserRequest userRequest) {
        User user = new User();
        user.setNombre(userRequest.getNombre());
        user.setEmail(userRequest.getEmail());
        user.setPassword(userRequest.getPassword()); //  En el futuro cifrar con BCrypt u otra cosa
        user.setFechaCreacion(LocalDateTime.now());
        user.setActivo(true);

        return userRepository.save(user);
    }

    // Método adicional para GraphQL: registrar sin DTO
    public User registerUser(String nombre, String email, String password) {
        User user = new User();
        user.setNombre(nombre);
        user.setEmail(email);
        user.setPassword(password); // ⚠️ En el futuro cifrar con BCrypt
        user.setFechaCreacion(LocalDateTime.now());
        user.setActivo(true);

        return userRepository.save(user);
    }

    //  Método adicional para GraphQL: buscar por email
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}

