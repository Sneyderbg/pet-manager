package com.petmanager.auth_service.service;

import com.petmanager.auth_service.dto.UserRequest;
import com.petmanager.auth_service.model.User;
import com.petmanager.auth_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User registerUser(UserRequest userRequest) {
        // Crear nuevo usuario a partir del DTO
        User user = new User();
        user.setNombre(userRequest.getNombre());
        user.setEmail(userRequest.getEmail());
        user.setPassword(userRequest.getPassword()); // ⚠️ En el futuro cifrar con BCrypt
        user.setFechaCreacion(LocalDateTime.now());
        user.setActivo(true);

        return userRepository.save(user);
    }
}
