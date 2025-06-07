package com.petmanager.supplier_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CondicionPagoVencimientoDto {
    private Long idCondicionPago;
    private Long idProveedor;
    private String nombreProveedor;
    private String emailProveedor;
    private Long idUsuario;
    private Integer diasCredito;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String nota;
    private Integer diasRestantes;
}