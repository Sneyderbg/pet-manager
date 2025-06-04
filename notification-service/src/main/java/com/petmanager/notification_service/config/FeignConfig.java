package com.petmanager.notification_service.config;

import feign.Logger;
import feign.Request;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuración de Feign Client para comunicación con microservicios
 */
@Configuration
public class FeignConfig {

    /**
     * Configuración de timeouts para llamadas a supplier-service
     */
    @Bean
    public Request.Options feignRequestOptions() {
        return new Request.Options(
                5000,  // connect timeout
                10000  // read timeout
        );
    }

    /**
     * Configuración de reintentos para tolerancia a fallos
     */
    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(
                1000,    // Tiempo inicial entre reintentos (1 segundo)
                3000,    // Tiempo máximo entre reintentos (3 segundos)
                3        // Máximo número de reintentos
        );
    }

    /**
     * Logging para debugging (solo en desarrollo)
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC; // NONE, BASIC, HEADERS, FULL
    }
}