package com.petmanager.supplier_service.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProveedorInput {
    private String nombre;
    private String nit;
    private String direccion;
    private String telefono;
    private String email;
    private Long idUsuarioCreador;
    private List<ProductoInput> productos;
    private List<CondicionPagoInput> condicionesPago;
}
