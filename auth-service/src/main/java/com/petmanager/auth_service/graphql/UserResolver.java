package com.petmanager.auth_service.graphql;

import com.petmanager.auth_service.model.User;
import com.petmanager.auth_service.repository.UserRepository;
import com.petmanager.auth_service.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class UserResolver {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @QueryMapping
    public User getUserByEmail(@Argument String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @MutationMapping
    public String login(@Argument String email, @Argument String password) {
        return userRepository.findByEmail(email)
                .map(user -> {
                    if (!user.getPassword().equals(password)) {
                        return "Error: Credenciales inválidas";
                    }
                    return jwtService.generateToken(user.getEmail());
                })
                .orElse("Error: Usuario no encontrado");
    }
}
