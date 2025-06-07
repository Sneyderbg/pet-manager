package com.petmanager.supplier_service.graphql;

import com.petmanager.supplier_service.dto.CondicionPagoInput;
import com.petmanager.supplier_service.model.CondicionPago;
import com.petmanager.supplier_service.service.CondicionPagoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;

@Controller
public class CondicionPagoResolver {

    @Autowired
    private CondicionPagoService condicionPagoService;

    @MutationMapping
    public CondicionPago actualizarCondicionPago(
            @Argument Long idCondicionPago,
            @Argument Integer diasCredito,
            @Argument String fechaInicio,
            @Argument String fechaFin,
            @Argument String nota
    ) {
        // Crear objeto con los datos a actualizar
        CondicionPago datosActualizacion = new CondicionPago();

        if (diasCredito != null) {
            datosActualizacion.setDiasCredito(diasCredito);
        }

        if (fechaInicio != null) {
            datosActualizacion.setFechaInicio(LocalDate.parse(fechaInicio));
        }

        if (fechaFin != null) {
            datosActualizacion.setFechaFin(LocalDate.parse(fechaFin));
        }

        if (nota != null) {
            datosActualizacion.setNota(nota);
        }

        // Usar nuestro servicio actualizado con excepciones
        return condicionPagoService.actualizarCondicionPago(idCondicionPago, datosActualizacion);
    }

    @MutationMapping
    public Boolean eliminarCondicionPago(@Argument Long idCondicionPago) {
        return condicionPagoService.eliminarCondicionPago(idCondicionPago);
    }

    @MutationMapping
    public CondicionPago crearCondicionPago(@Argument CondicionPagoInput input) {
        return condicionPagoService.crearCondicionPago(input);
    }

    // ================================================
    // NUEVA MUTATION: Crear condición asociada a proveedor
    // ================================================

    @MutationMapping
    public CondicionPago crearCondicionPagoParaProveedor(
            @Argument Long idProveedor,
            @Argument CondicionPagoInput input
    ) {
        return condicionPagoService.crearCondicionPagoParaProveedor(idProveedor, input);
    }
}