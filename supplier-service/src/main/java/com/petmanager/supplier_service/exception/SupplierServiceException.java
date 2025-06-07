package com.petmanager.supplier_service.exception;

public class SupplierServiceException extends RuntimeException {
    public SupplierServiceException(String message) {
        super(message);
    }

    public SupplierServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}