package com.petmanager.notification_service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Configuración del sistema de scheduling para notificaciones
 */
@Configuration
@EnableScheduling
@Slf4j
public class SchedulerConfig {

    @Value("${notifications.scheduler.pool-size:3}")
    private int poolSize;

    @Value("${notifications.scheduler.thread-name-prefix:notification-scheduler-}")
    private String threadNamePrefix;

    @Value("${notifications.scheduler.await-termination-seconds:60}")
    private int awaitTerminationSeconds;

    /**
     * Configuración del TaskScheduler para las tareas programadas
     */
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

        // Configuración del pool de threads
        scheduler.setPoolSize(poolSize);
        scheduler.setThreadNamePrefix(threadNamePrefix);
        scheduler.setAwaitTerminationSeconds(awaitTerminationSeconds);

        // Configuración adicional
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setRemoveOnCancelPolicy(true);

        // Manejador de errores personalizado
        scheduler.setErrorHandler(throwable -> {
            log.error("💥 Error en tarea programada: {}", throwable.getMessage(), throwable);
        });

        // Inicializar
        scheduler.initialize();

        log.info("⚡ TaskScheduler configurado:");
        log.info("   🔢 Pool Size: {}", poolSize);
        log.info("   🏷️ Thread Prefix: {}", threadNamePrefix);
        log.info("   ⏱️ Await Termination: {} segundos", awaitTerminationSeconds);

        return scheduler;
    }
}