package com.petmanager.notification_service.client;

import com.petmanager.notification_service.dto.CondicionPagoVencimientoDto;
import com.petmanager.notification_service.dto.ProveedorNotificacionDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Feign Client para comunicación con supplier-service
 * Consume los endpoints REST que ya tienes implementados
 */
@FeignClient(
        name = "supplier-service",
        url = "${services.supplier-service.url}",
        path = "/api"
)
public interface SupplierServiceClient {

    /**
     * Obtiene condiciones próximas a vencer
     * Consume: GET /api/condiciones-pago/proximas-vencer?dias=7,3,1,0
     */
    @GetMapping("/condiciones-pago/proximas-vencer")
    ResponseEntity<List<CondicionPagoVencimientoDto>> obtenerCondicionesProximasAVencer(
            @RequestParam(value = "dias", defaultValue = "7,3,1,0") String dias
    );

    /**
     * Obtiene información de un proveedor específico para notificaciones
     * Consume: GET /api/proveedores/{id}/info-notificacion
     */
    @GetMapping("/proveedores/{id}/info-notificacion")
    ResponseEntity<ProveedorNotificacionDto> obtenerInfoProveedor(@PathVariable("id") Long id);

    /**
     * Health check del supplier-service
     * Consume: GET /api/health
     */
    @GetMapping("/health")
    ResponseEntity<String> healthCheck();
}