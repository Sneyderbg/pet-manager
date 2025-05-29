package com.petmanager.supplier_service.exception;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para GraphQL.
 * Intercepta todas las excepciones y las convierte en respuestas
 * estructuradas y amigables para el cliente.
 */
@Component
public class GlobalExceptionHandler extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        Map<String, Object> extensions = new HashMap<>();
        extensions.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        extensions.put("path", env.getExecutionStepInfo().getPath().toString());

        ErrorType errorType;
        String message;
        String errorCode;

        if (ex instanceof DuplicateResourceException) {
            errorType = ErrorType.BAD_REQUEST;
            message = ex.getMessage();
            errorCode = "DUPLICATE_RESOURCE";
            extensions.put("errorCode", errorCode);
            extensions.put("suggestion", "Verifique que los datos no estén duplicados");
            extensions.put("severity", "MEDIUM");

        } else if (ex instanceof ResourceNotFoundException) {
            errorType = ErrorType.NOT_FOUND;
            message = ex.getMessage();
            errorCode = "RESOURCE_NOT_FOUND";
            extensions.put("errorCode", errorCode);
            extensions.put("suggestion", "Verifique que el ID proporcionado sea válido");
            extensions.put("severity", "LOW");

        } else if (ex instanceof BusinessValidationException) {
            errorType = ErrorType.BAD_REQUEST;
            message = ex.getMessage();
            errorCode = "BUSINESS_VALIDATION_ERROR";
            extensions.put("errorCode", errorCode);
            extensions.put("suggestion", "Revise las reglas de negocio aplicables");
            extensions.put("severity", "MEDIUM");

        } else if (ex instanceof InvalidDataException) {
            errorType = ErrorType.BAD_REQUEST;
            message = ex.getMessage();
            errorCode = "INVALID_DATA";
            extensions.put("errorCode", errorCode);
            extensions.put("suggestion", "Verifique que todos los campos tengan valores válidos");
            extensions.put("severity", "HIGH");

        } else if (ex instanceof IllegalArgumentException) {
            errorType = ErrorType.BAD_REQUEST;
            message = "Datos de entrada inválidos: " + ex.getMessage();
            errorCode = "INVALID_INPUT";
            extensions.put("errorCode", errorCode);
            extensions.put("suggestion", "Revise los parámetros enviados");
            extensions.put("severity", "HIGH");

        } else {
            // Excepciones no controladas
            errorType = ErrorType.INTERNAL_ERROR;
            message = "Error interno del servidor. Contacte al administrador.";
            errorCode = "INTERNAL_ERROR";
            extensions.put("errorCode", errorCode);
            extensions.put("originalError", ex.getMessage());
            extensions.put("severity", "CRITICAL");
            extensions.put("supportContact", "soporte@petmanager.com");

            // Log para debugging
            System.err.println("Error no controlado en " + env.getExecutionStepInfo().getPath() + ": " + ex.getMessage());
            ex.printStackTrace();
        }

        return GraphqlErrorBuilder.newError()
                .message(message)
                .errorType(errorType)
                .extensions(extensions)
                .path(env.getExecutionStepInfo().getPath())
                .build();
    }
}