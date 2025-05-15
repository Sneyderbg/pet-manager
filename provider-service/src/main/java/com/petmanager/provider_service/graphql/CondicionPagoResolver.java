package com.petmanager.provider_service.graphql;

import com.petmanager.provider_service.model.CondicionPago;
import com.petmanager.provider_service.service.CondicionPagoService;
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
        CondicionPago nueva = new CondicionPago();
        nueva.setDiasCredito(diasCredito);
        nueva.setFechaInicio(LocalDate.parse(fechaInicio));
        nueva.setFechaFin(LocalDate.parse(fechaFin));
        nueva.setNota(nota);

        return condicionPagoService.actualizarCondicionPago(idCondicionPago, nueva);
    }

    @MutationMapping
    public Boolean eliminarCondicionPago(@Argument Long idCondicionPago) {
        return condicionPagoService.eliminarCondicionPago(idCondicionPago);
    }
}
