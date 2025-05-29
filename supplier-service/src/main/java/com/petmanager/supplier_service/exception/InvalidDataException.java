package com.petmanager.supplier_service.exception;

public class InvalidDataException extends SupplierServiceException {
    public InvalidDataException(String message) {
        super(message);
    }

    public InvalidDataException(String message, Throwable cause) {
        super(message, cause);
    }
}