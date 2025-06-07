package com.petmanager.supplier_service.service;

import com.petmanager.supplier_service.dto.CondicionPagoInput;
import com.petmanager.supplier_service.exception.*;
import com.petmanager.supplier_service.model.CondicionPago;
import com.petmanager.supplier_service.model.Proveedor;
import com.petmanager.supplier_service.repository.CondicionPagoRepository;
import com.petmanager.supplier_service.repository.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Service
public class CondicionPagoService {

    @Autowired
    private CondicionPagoRepository condicionPagoRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    // Actualizar condición de pago existente
    public CondicionPago actualizarCondicionPago(Long id, CondicionPago nuevaCondicion) {
        // Validaciones de entrada
        if (id == null || id <= 0) {
            throw new InvalidDataException("El ID de la condición de pago debe ser un número positivo");
        }

        if (nuevaCondicion == null) {
            throw new InvalidDataException("Los datos de la condición de pago no pueden estar vacíos");
        }

        // Buscar condición existente
        CondicionPago existente = condicionPagoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Condición de pago no encontrada con ID: " + id));

        // Verificar que el proveedor esté activo
        if (existente.getProveedor() != null && !existente.getProveedor().getActivo()) {
            throw new BusinessValidationException("No se pueden modificar condiciones de pago de un proveedor inactivo");
        }

        // Validar y actualizar campos
        if (nuevaCondicion.getDiasCredito() != null) {
            if (nuevaCondicion.getDiasCredito() < 0) {
                throw new InvalidDataException("Los días de crédito no pueden ser negativos");
            }
            if (nuevaCondicion.getDiasCredito() > 365) {
                throw new BusinessValidationException("Los días de crédito no pueden exceder 365 días");
            }
            existente.setDiasCredito(nuevaCondicion.getDiasCredito());
        }

        if (nuevaCondicion.getFechaInicio() != null) {
            // Validar que la fecha de inicio no sea muy antigua
            if (nuevaCondicion.getFechaInicio().isBefore(LocalDate.now().minusYears(2))) {
                throw new BusinessValidationException("La fecha de inicio no puede ser anterior a 2 años atrás");
            }
            existente.setFechaInicio(nuevaCondicion.getFechaInicio());
        }

        if (nuevaCondicion.getFechaFin() != null) {
            existente.setFechaFin(nuevaCondicion.getFechaFin());
        }

        // Validar fechas después de establecerlas
        if (existente.getFechaInicio() != null && existente.getFechaFin() != null) {
            if (existente.getFechaInicio().isAfter(existente.getFechaFin())) {
                throw new BusinessValidationException("La fecha de inicio no puede ser posterior a la fecha de fin");
            }

            // Validar que el período no sea demasiado largo (más de 5 años)
            if (existente.getFechaInicio().plusYears(5).isBefore(existente.getFechaFin())) {
                throw new BusinessValidationException("El período de la condición de pago no puede exceder 5 años");
            }
        }

        if (nuevaCondicion.getNota() != null) {
            String nota = StringUtils.hasText(nuevaCondicion.getNota()) ?
                    nuevaCondicion.getNota().trim() : "";

            if (nota.length() > 500) {
                throw new InvalidDataException("La nota no puede tener más de 500 caracteres");
            }
            existente.setNota(nota);
        }

        return condicionPagoRepository.save(existente);
    }

    // Eliminar condición de pago
    public boolean eliminarCondicionPago(Long id) {
        // Validación de entrada
        if (id == null || id <= 0) {
            throw new InvalidDataException("El ID de la condición de pago debe ser un número positivo");
        }

        CondicionPago condicion = condicionPagoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe una condición de pago con ID: " + id));

        // Verificar que el proveedor esté activo
        if (condicion.getProveedor() != null && !condicion.getProveedor().getActivo()) {
            throw new BusinessValidationException("No se pueden eliminar condiciones de pago de un proveedor inactivo");
        }

        // Validar si la condición está vigente
        LocalDate hoy = LocalDate.now();
        if (condicion.getFechaInicio() != null && condicion.getFechaFin() != null) {
            if (!hoy.isBefore(condicion.getFechaInicio()) && !hoy.isAfter(condicion.getFechaFin())) {
                throw new BusinessValidationException("No se puede eliminar una condición de pago que está vigente actualmente");
            }
        }

        condicionPagoRepository.deleteById(id);
        return true;
    }

    // Crear nueva condición de pago
    public CondicionPago crearCondicionPago(CondicionPagoInput input) {
        // Validación de entrada
        if (input == null) {
            throw new InvalidDataException("Los datos de la condición de pago no pueden estar vacíos");
        }

        // Validar campos obligatorios
        validarCamposObligatorios(input);

        // Validar valores
        validarValores(input);

        // Validar fechas
        validarFechas(input);

        CondicionPago nueva = CondicionPago.builder()
                .diasCredito(input.getDiasCredito())
                .fechaInicio(input.getFechaInicio())
                .fechaFin(input.getFechaFin())
                .nota(StringUtils.hasText(input.getNota()) ? input.getNota().trim() : "")
                .idUsuario(input.getIdUsuario())
                .build();

        return condicionPagoRepository.save(nueva);
    }

    // Crear condición de pago asociada a un proveedor
    public CondicionPago crearCondicionPagoParaProveedor(Long idProveedor, CondicionPagoInput input) {
        // Validaciones de entrada
        if (idProveedor == null || idProveedor <= 0) {
            throw new InvalidDataException("El ID del proveedor debe ser un número positivo");
        }

        if (input == null) {
            throw new InvalidDataException("Los datos de la condición de pago no pueden estar vacíos");
        }

        // Verificar que el proveedor existe y está activo
        Proveedor proveedor = proveedorRepository.findById(idProveedor)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado con ID: " + idProveedor));

        if (!proveedor.getActivo()) {
            throw new BusinessValidationException("No se pueden crear condiciones de pago para un proveedor inactivo");
        }

        // Validar campos obligatorios
        validarCamposObligatorios(input);

        // Validar valores
        validarValores(input);

        // Validar fechas
        validarFechas(input);

        // Verificar si ya existe una condición similar (mismos días de crédito y fechas solapadas)
        validarCondicionDuplicada(proveedor, input);

        CondicionPago nueva = CondicionPago.builder()
                .proveedor(proveedor)
                .diasCredito(input.getDiasCredito())
                .fechaInicio(input.getFechaInicio())
                .fechaFin(input.getFechaFin())
                .nota(StringUtils.hasText(input.getNota()) ? input.getNota().trim() : "")
                .idUsuario(input.getIdUsuario())
                .build();

        return condicionPagoRepository.save(nueva);
    }

    // Obtener condición de pago por ID
    public CondicionPago obtenerPorId(Long id) {
        if (id == null || id <= 0) {
            throw new InvalidDataException("El ID de la condición de pago debe ser un número positivo");
        }

        return condicionPagoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Condición de pago no encontrada con ID: " + id));
    }

    // Listar condiciones de pago por proveedor
    public List<CondicionPago> listarPorProveedor(Long idProveedor) {
        if (idProveedor == null || idProveedor <= 0) {
            throw new InvalidDataException("El ID del proveedor debe ser un número positivo");
        }

        // Verificar que el proveedor existe
        if (!proveedorRepository.existsById(idProveedor)) {
            throw new ResourceNotFoundException("Proveedor no encontrado con ID: " + idProveedor);
        }

        List<CondicionPago> condiciones = condicionPagoRepository.findAll().stream()
                .filter(c -> c.getProveedor() != null && c.getProveedor().getIdProveedor().equals(idProveedor))
                .toList();

        if (condiciones.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron condiciones de pago para el proveedor con ID: " + idProveedor);
        }

        return condiciones;
    }

    // Listar condiciones de pago vigentes por proveedor
    public List<CondicionPago> listarVigentesPorProveedor(Long idProveedor) {
        if (idProveedor == null || idProveedor <= 0) {
            throw new InvalidDataException("El ID del proveedor debe ser un número positivo");
        }

        // Verificar que el proveedor existe
        if (!proveedorRepository.existsById(idProveedor)) {
            throw new ResourceNotFoundException("Proveedor no encontrado con ID: " + idProveedor);
        }

        LocalDate hoy = LocalDate.now();
        List<CondicionPago> condicionesVigentes = condicionPagoRepository.findAll().stream()
                .filter(c -> c.getProveedor() != null &&
                        c.getProveedor().getIdProveedor().equals(idProveedor) &&
                        c.getFechaInicio() != null &&
                        c.getFechaFin() != null &&
                        !hoy.isBefore(c.getFechaInicio()) &&
                        !hoy.isAfter(c.getFechaFin()))
                .toList();

        if (condicionesVigentes.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron condiciones de pago vigentes para el proveedor con ID: " + idProveedor);
        }

        return condicionesVigentes;
    }

    // ==============================================
    // MÉTODOS PRIVADOS DE VALIDACIÓN
    // ==============================================

    private void validarCamposObligatorios(CondicionPagoInput input) {
        if (input.getDiasCredito() == null) {
            throw new InvalidDataException("Los días de crédito son obligatorios");
        }

        if (input.getFechaInicio() == null) {
            throw new InvalidDataException("La fecha de inicio es obligatoria");
        }

        if (input.getFechaFin() == null) {
            throw new InvalidDataException("La fecha de fin es obligatoria");
        }

        if (input.getIdUsuario() == null) {
            throw new InvalidDataException("El ID del usuario es obligatorio");
        }
    }

    private void validarValores(CondicionPagoInput input) {
        if (input.getDiasCredito() < 0) {
            throw new InvalidDataException("Los días de crédito no pueden ser negativos");
        }

        if (input.getDiasCredito() > 365) {
            throw new BusinessValidationException("Los días de crédito no pueden exceder 365 días");
        }

        if (input.getIdUsuario() <= 0) {
            throw new InvalidDataException("El ID del usuario debe ser un número positivo");
        }

        // Validar longitud de la nota si está presente
        if (StringUtils.hasText(input.getNota()) && input.getNota().length() > 500) {
            throw new InvalidDataException("La nota no puede tener más de 500 caracteres");
        }
    }

    private void validarFechas(CondicionPagoInput input) {
        // Validar que fecha inicio no sea muy antigua
        if (input.getFechaInicio().isBefore(LocalDate.now().minusYears(2))) {
            throw new BusinessValidationException("La fecha de inicio no puede ser anterior a 2 años atrás");
        }

        // Validar que fecha fin no sea muy en el futuro
        if (input.getFechaFin().isAfter(LocalDate.now().plusYears(10))) {
            throw new BusinessValidationException("La fecha de fin no puede ser superior a 10 años en el futuro");
        }

        // Validar orden de fechas
        if (input.getFechaInicio().isAfter(input.getFechaFin())) {
            throw new BusinessValidationException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }

        // Validar que el período no sea demasiado corto (mínimo 1 día)
        if (input.getFechaInicio().equals(input.getFechaFin())) {
            throw new BusinessValidationException("El período de la condición de pago debe ser de al menos 1 día");
        }

        // Validar que el período no sea demasiado largo (máximo 5 años)
        if (input.getFechaInicio().plusYears(5).isBefore(input.getFechaFin())) {
            throw new BusinessValidationException("El período de la condición de pago no puede exceder 5 años");
        }
    }

    private void validarCondicionDuplicada(Proveedor proveedor, CondicionPagoInput input) {
        // Buscar condiciones existentes del proveedor que se solapen en fechas y tengan los mismos días de crédito
        boolean existeCondicionSimilar = proveedor.getCondicionesPago().stream()
                .anyMatch(c -> c.getDiasCredito().equals(input.getDiasCredito()) &&
                        fechasSeSolapan(c.getFechaInicio(), c.getFechaFin(),
                                input.getFechaInicio(), input.getFechaFin()));

        if (existeCondicionSimilar) {
            throw new DuplicateResourceException("Ya existe una condición de pago con " + input.getDiasCredito() +
                    " días de crédito que se solapa con el período especificado");
        }
    }

    private boolean fechasSeSolapan(LocalDate inicio1, LocalDate fin1, LocalDate inicio2, LocalDate fin2) {
        return !(fin1.isBefore(inicio2) || inicio1.isAfter(fin2));
    }
}