package com.petmanager.auth_service.graphql;

import com.petmanager.auth_service.model.User;
import com.petmanager.auth_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class UserResolver {

    @Autowired
    private UserService userService;

    @QueryMapping
    public User getUserByEmail(@Argument String email) {
        return userService.findByEmail(email)
                .orElse(null); // podrías lanzar excepción si prefieres
    }

    @MutationMapping
    public User registerUser(@Argument String nombre,
                             @Argument String email,
                             @Argument String password) {
        return userService.registerUser(nombre, email, password);
    }
}
