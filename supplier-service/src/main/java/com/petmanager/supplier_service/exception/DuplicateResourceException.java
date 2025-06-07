package com.petmanager.supplier_service.exception;

public class DuplicateResourceException extends SupplierServiceException {
    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}