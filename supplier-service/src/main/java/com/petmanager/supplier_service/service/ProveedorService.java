package com.petmanager.supplier_service.service;

import com.petmanager.supplier_service.dto.ProveedorInput;
import com.petmanager.supplier_service.exception.*;
import com.petmanager.supplier_service.model.CondicionPago;
import com.petmanager.supplier_service.model.Producto;
import com.petmanager.supplier_service.model.Proveedor;
import com.petmanager.supplier_service.repository.CondicionPagoRepository;
import com.petmanager.supplier_service.repository.ProductoRepository;
import com.petmanager.supplier_service.repository.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProveedorService {

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CondicionPagoRepository condicionPagoRepository;

    // Crear proveedor con productos y condiciones de pago
    public Proveedor crearProveedor(ProveedorInput input) {
        // Validación de entrada
        if (input == null) {
            throw new InvalidDataException("Los datos del proveedor no pueden estar vacíos");
        }

        // Validaciones de campos obligatorios
        validarCamposObligatorios(input);

        // Validaciones de formato
        validarFormatos(input);

        // Validaciones de unicidad
        validarUnicidad(input.getNit(), input.getEmail(), null);

        // Crear y guardar proveedor
        Proveedor proveedor = construirProveedor(input);
        Proveedor proveedorGuardado = proveedorRepository.save(proveedor);

        // Asociar productos
        if (input.getProductos() != null && !input.getProductos().isEmpty()) {
            procesarProductos(input, proveedorGuardado);
        }

        // Asociar condiciones de pago
        if (input.getCondicionesPago() != null && !input.getCondicionesPago().isEmpty()) {
            procesarCondicionesPago(input, proveedorGuardado);
        }

        return proveedorGuardado;
    }

    // Obtener todos los proveedores
    public List<Proveedor> getAll() {
        List<Proveedor> proveedores = proveedorRepository.findAll();
        if (proveedores.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron proveedores registrados");
        }
        return proveedores;
    }

    // Obtener proveedor por ID
    public Proveedor getById(Long id) {
        if (id == null || id <= 0) {
            throw new InvalidDataException("El ID del proveedor debe ser un número positivo");
        }

        return proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado con ID: " + id));
    }

    // Actualizar proveedor
    public Proveedor update(Long id, String nombre, String nit, String direccion,
                            String telefono, String email, Boolean activo) {
        Proveedor proveedor = getById(id);

        // Validar unicidad si se están actualizando NIT o email
        if (StringUtils.hasText(nit) || StringUtils.hasText(email)) {
            validarUnicidad(nit, email, id);
        }

        // Validar formatos si se están actualizando
        if (StringUtils.hasText(email) && !esEmailValido(email)) {
            throw new InvalidDataException("El formato del email no es válido");
        }

        if (StringUtils.hasText(nit) && !esNitValido(nit)) {
            throw new InvalidDataException("El formato del NIT no es válido");
        }

        // Actualizar campos
        if (StringUtils.hasText(nombre)) {
            if (nombre.trim().length() > 100) {
                throw new InvalidDataException("El nombre no puede tener más de 100 caracteres");
            }
            proveedor.setNombre(nombre.trim());
        }

        if (StringUtils.hasText(nit)) proveedor.setNit(nit.trim());
        if (StringUtils.hasText(direccion)) {
            if (direccion.trim().length() > 200) {
                throw new InvalidDataException("La dirección no puede tener más de 200 caracteres");
            }
            proveedor.setDireccion(direccion.trim());
        }
        if (StringUtils.hasText(telefono)) {
            if (telefono.trim().length() > 20) {
                throw new InvalidDataException("El teléfono no puede tener más de 20 caracteres");
            }
            proveedor.setTelefono(telefono.trim());
        }
        if (StringUtils.hasText(email)) proveedor.setEmail(email.toLowerCase().trim());
        if (activo != null) proveedor.setActivo(activo);

        return proveedorRepository.save(proveedor);
    }

    // Borrado lógico
    public boolean delete(Long id) {
        Proveedor proveedor = getById(id);

        if (!proveedor.getActivo()) {
            throw new BusinessValidationException("El proveedor con ID " + id + " ya está inactivo");
        }

        proveedor.setActivo(false);
        proveedorRepository.save(proveedor);
        return true;
    }

    // ==============================================
    // MÉTODOS PRIVADOS DE VALIDACIÓN
    // ==============================================

    private void validarCamposObligatorios(ProveedorInput input) {
        if (!StringUtils.hasText(input.getNombre())) {
            throw new InvalidDataException("El nombre del proveedor es obligatorio");
        }
        if (!StringUtils.hasText(input.getNit())) {
            throw new InvalidDataException("El NIT del proveedor es obligatorio");
        }
        if (!StringUtils.hasText(input.getEmail())) {
            throw new InvalidDataException("El email del proveedor es obligatorio");
        }
        if (!StringUtils.hasText(input.getDireccion())) {
            throw new InvalidDataException("La dirección del proveedor es obligatoria");
        }
        if (!StringUtils.hasText(input.getTelefono())) {
            throw new InvalidDataException("El teléfono del proveedor es obligatorio");
        }
        if (input.getIdUsuarioCreador() == null) {
            throw new InvalidDataException("El ID del usuario creador es obligatorio");
        }
    }

    private void validarFormatos(ProveedorInput input) {
        if (!esEmailValido(input.getEmail())) {
            throw new InvalidDataException("El formato del email no es válido");
        }

        if (!esNitValido(input.getNit())) {
            throw new InvalidDataException("El formato del NIT no es válido (debe contener solo números y guiones)");
        }

        // Validar longitudes
        if (input.getNombre().trim().length() > 100) {
            throw new InvalidDataException("El nombre no puede tener más de 100 caracteres");
        }

        if (input.getDireccion().trim().length() > 200) {
            throw new InvalidDataException("La dirección no puede tener más de 200 caracteres");
        }

        if (input.getTelefono().trim().length() > 20) {
            throw new InvalidDataException("El teléfono no puede tener más de 20 caracteres");
        }

        if (input.getEmail().trim().length() > 150) {
            throw new InvalidDataException("El email no puede tener más de 150 caracteres");
        }
    }

    private void validarUnicidad(String nit, String email, Long idExcluir) {
        // Validar NIT único
        if (StringUtils.hasText(nit)) {
            boolean nitExiste = proveedorRepository.findAll().stream()
                    .anyMatch(p -> p.getNit().equals(nit.trim()) &&
                            (idExcluir == null || !p.getIdProveedor().equals(idExcluir)));

            if (nitExiste) {
                throw new DuplicateResourceException("Ya existe un proveedor registrado con el NIT: " + nit);
            }
        }

        // Validar email único
        if (StringUtils.hasText(email)) {
            boolean emailExiste = proveedorRepository.findAll().stream()
                    .anyMatch(p -> p.getEmail().equalsIgnoreCase(email.trim()) &&
                            (idExcluir == null || !p.getIdProveedor().equals(idExcluir)));

            if (emailExiste) {
                throw new DuplicateResourceException("Ya existe un proveedor registrado con el email: " + email);
            }
        }
    }

    private boolean esEmailValido(String email) {
        return StringUtils.hasText(email) &&
                email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    }

    private boolean esNitValido(String nit) {
        return StringUtils.hasText(nit) &&
                nit.matches("^[0-9-]+$") &&
                nit.length() >= 8 &&
                nit.length() <= 15;
    }

    private Proveedor construirProveedor(ProveedorInput input) {
        return Proveedor.builder()
                .nombre(input.getNombre().trim())
                .nit(input.getNit().trim())
                .direccion(input.getDireccion().trim())
                .telefono(input.getTelefono().trim())
                .email(input.getEmail().toLowerCase().trim())
                .fechaRegistro(LocalDateTime.now())
                .activo(true)
                .idUsuarioCreador(input.getIdUsuarioCreador())
                .build();
    }

    private void procesarProductos(ProveedorInput input, Proveedor proveedorGuardado) {
        // Validar códigos únicos en la lista
        List<String> codigos = input.getProductos().stream()
                .map(p -> p.getCodigo())
                .collect(Collectors.toList());

        long codigosUnicos = codigos.stream().distinct().count();
        if (codigosUnicos != codigos.size()) {
            throw new DuplicateResourceException("Hay códigos de productos duplicados en la lista");
        }

        // Crear productos
        List<Producto> productos = input.getProductos().stream().map(productoInput -> {
            // Validar campos obligatorios del producto
            if (!StringUtils.hasText(productoInput.getCodigo())) {
                throw new InvalidDataException("El código del producto es obligatorio");
            }
            if (!StringUtils.hasText(productoInput.getNombre())) {
                throw new InvalidDataException("El nombre del producto es obligatorio");
            }
            if (productoInput.getPrecio() == null) {
                throw new InvalidDataException("El precio del producto es obligatorio");
            }
            if (productoInput.getPrecio() < 0) {
                throw new InvalidDataException("El precio del producto no puede ser negativo");
            }

            // Validar que el código no exista globalmente
            if (productoRepository.existsByCodigo(productoInput.getCodigo())) {
                throw new DuplicateResourceException("Ya existe un producto con el código: " + productoInput.getCodigo());
            }

            Producto producto = new Producto();
            producto.setCodigo(productoInput.getCodigo().trim());
            producto.setNombre(productoInput.getNombre().trim());
            producto.setDescripcion(StringUtils.hasText(productoInput.getDescripcion()) ?
                    productoInput.getDescripcion().trim() : "");
            producto.setPrecio(productoInput.getPrecio());
            producto.setProveedor(proveedorGuardado);
            return producto;
        }).collect(Collectors.toList());

        productoRepository.saveAll(productos);
    }

    private void procesarCondicionesPago(ProveedorInput input, Proveedor proveedorGuardado) {
        List<CondicionPago> condiciones = input.getCondicionesPago().stream().map(condInput -> {
            // Validar campos obligatorios
            if (condInput.getDiasCredito() == null) {
                throw new InvalidDataException("Los días de crédito son obligatorios");
            }
            if (condInput.getFechaInicio() == null) {
                throw new InvalidDataException("La fecha de inicio es obligatoria");
            }
            if (condInput.getFechaFin() == null) {
                throw new InvalidDataException("La fecha de fin es obligatoria");
            }
            if (condInput.getIdUsuario() == null) {
                throw new InvalidDataException("El ID del usuario es obligatorio");
            }

            // Validaciones de datos
            if (condInput.getDiasCredito() < 0) {
                throw new InvalidDataException("Los días de crédito no pueden ser negativos");
            }
            if (condInput.getDiasCredito() > 365) {
                throw new BusinessValidationException("Los días de crédito no pueden exceder 365 días");
            }

            // Validaciones de fechas
            if (condInput.getFechaInicio().isAfter(condInput.getFechaFin())) {
                throw new BusinessValidationException("La fecha de inicio no puede ser posterior a la fecha de fin");
            }

            CondicionPago cond = new CondicionPago();
            cond.setProveedor(proveedorGuardado);
            cond.setDiasCredito(condInput.getDiasCredito());
            cond.setFechaInicio(condInput.getFechaInicio());
            cond.setFechaFin(condInput.getFechaFin());
            cond.setNota(StringUtils.hasText(condInput.getNota()) ? condInput.getNota().trim() : "");
            cond.setIdUsuario(condInput.getIdUsuario());
            return cond;
        }).collect(Collectors.toList());

        condicionPagoRepository.saveAll(condiciones);
    }
}
