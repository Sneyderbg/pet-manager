package com.petmanager.supplier_service.exception;

public class BusinessValidationException extends SupplierServiceException {
    public BusinessValidationException(String message) {
        super(message);
    }

    public BusinessValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}