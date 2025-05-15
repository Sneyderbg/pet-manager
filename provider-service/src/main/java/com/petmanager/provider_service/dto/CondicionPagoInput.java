package com.petmanager.provider_service.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CondicionPagoInput {
    private Integer diasCredito;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String nota;
}
