package com.petmanager.supplier_service.exception;

public class ResourceNotFoundException extends SupplierServiceException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}