package com.petmanager.auth_service.controller;

import com.petmanager.auth_service.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test-email")
public class EmailTestController {

    @Autowired
    private EmailService emailService;

    @PostMapping
    public String enviarCorreoPrueba(@RequestParam String to,
                                     @RequestParam String subject,
                                     @RequestParam String message) {
        emailService.enviarCorreo(to, subject, message);
        return "Correo enviado a " + to;
    }
}
