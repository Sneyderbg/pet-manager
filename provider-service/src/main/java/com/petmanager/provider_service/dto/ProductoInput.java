package com.petmanager.provider_service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductoInput {
    private String codigo;
    private String nombre;
    private String descripcion;
    private Double precio;
}
