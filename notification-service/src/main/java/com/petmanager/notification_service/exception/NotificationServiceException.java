package com.petmanager.notification_service.exception;

/**
 * Excepción base para el notification-service
 */
public class NotificationServiceException extends RuntimeException {

    public NotificationServiceException(String message) {
        super(message);
    }

    public NotificationServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}