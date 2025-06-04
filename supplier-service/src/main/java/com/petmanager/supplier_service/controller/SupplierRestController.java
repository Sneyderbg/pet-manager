package com.petmanager.supplier_service.controller;

import com.petmanager.supplier_service.dto.CondicionPagoVencimientoDto;
import com.petmanager.supplier_service.dto.ProveedorNotificacionDto;
import com.petmanager.supplier_service.model.CondicionPago;
import com.petmanager.supplier_service.repository.CondicionPagoRepository;
import com.petmanager.supplier_service.service.ProveedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller para comunicación entre microservicios
 */
@RestController
@RequestMapping("/api")
public class SupplierRestController {

    @Autowired
    private CondicionPagoRepository condicionPagoRepository;

    @Autowired
    private ProveedorService proveedorService;

    /**
     * Endpoint específico para notification-service
     * Obtiene condiciones próximas a vencer
     */
    @GetMapping("/condiciones-pago/proximas-vencer")
    public ResponseEntity<List<CondicionPagoVencimientoDto>> obtenerCondicionesProximasAVencer(
            @RequestParam(defaultValue = "7,3,1,0") String dias) {

        try {
            // Parsear días de alerta
            String[] diasArray = dias.split(",");
            List<Integer> diasAlerta = List.of(diasArray).stream()
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());

            // Obtener todas las condiciones directamente del repository
            LocalDate hoy = LocalDate.now();
            List<CondicionPago> todasCondiciones = condicionPagoRepository.findAll();

            // Filtrar por fechas próximas a vencer
            List<CondicionPagoVencimientoDto> condicionesProximas = todasCondiciones.stream()
                    .filter(c -> c.getFechaFin() != null && c.getProveedor() != null)
                    .filter(c -> c.getProveedor().getActivo()) // Solo proveedores activos
                    .map(c -> {
                        long diasRestantes = java.time.temporal.ChronoUnit.DAYS.between(hoy, c.getFechaFin());
                        return new CondicionPagoVencimientoDto(
                                c.getIdCondicionPago(),
                                c.getProveedor().getIdProveedor(),
                                c.getProveedor().getNombre(),
                                c.getProveedor().getEmail(),
                                c.getIdUsuario(),
                                c.getDiasCredito(),
                                c.getFechaInicio(),
                                c.getFechaFin(),
                                c.getNota(),
                                (int) diasRestantes
                        );
                    })
                    .filter(dto -> diasAlerta.contains(dto.getDiasRestantes()))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(condicionesProximas);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Endpoint para obtener datos de un proveedor específico
     * Útil para el notification-service
     */
    @GetMapping("/proveedores/{id}/info-notificacion")
    public ResponseEntity<ProveedorNotificacionDto> obtenerInfoProveedor(@PathVariable Long id) {
        try {
            var proveedor = proveedorService.getById(id);
            var dto = new ProveedorNotificacionDto(
                    proveedor.getIdProveedor(),
                    proveedor.getNombre(),
                    proveedor.getEmail(),
                    proveedor.getActivo()
            );
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Health check para el notification-service
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Supplier Service REST API OK");
    }
}