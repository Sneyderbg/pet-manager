package com.petmanager.provider_service.service;

import com.petmanager.provider_service.model.CondicionPago;
import com.petmanager.provider_service.repository.CondicionPagoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CondicionPagoService {

    @Autowired
    private CondicionPagoRepository condicionPagoRepository;

    public CondicionPago actualizarCondicionPago(Long id, CondicionPago nuevaCondicion) {
        CondicionPago existente = condicionPagoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Condición no encontrada con ID: " + id));

        if (nuevaCondicion.getDiasCredito() != null)
            existente.setDiasCredito(nuevaCondicion.getDiasCredito());

        if (nuevaCondicion.getFechaInicio() != null)
            existente.setFechaInicio(nuevaCondicion.getFechaInicio());

        if (nuevaCondicion.getFechaFin() != null)
            existente.setFechaFin(nuevaCondicion.getFechaFin());

        if (nuevaCondicion.getNota() != null)
            existente.setNota(nuevaCondicion.getNota());

        return condicionPagoRepository.save(existente);
    }

    public boolean eliminarCondicionPago(Long id) {
        if (!condicionPagoRepository.existsById(id)) {
            throw new RuntimeException("No existe una condición con ID: " + id);
        }

        condicionPagoRepository.deleteById(id);
        return true;
    }
}
