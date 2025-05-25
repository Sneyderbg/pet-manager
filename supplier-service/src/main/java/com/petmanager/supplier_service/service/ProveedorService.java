package com.petmanager.supplier_service.service;

import com.petmanager.supplier_service.dto.ProveedorInput;
import com.petmanager.supplier_service.model.CondicionPago;
import com.petmanager.supplier_service.model.Producto;
import com.petmanager.supplier_service.model.Proveedor;
import com.petmanager.supplier_service.repository.CondicionPagoRepository;
import com.petmanager.supplier_service.repository.ProductoRepository;
import com.petmanager.supplier_service.repository.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        if (input == null) {
            throw new IllegalArgumentException("El input del proveedor no puede ser nulo.");
        }

        // Validaciones de unicidad para NIT y email
        if (proveedorRepository.findAll().stream().anyMatch(p -> p.getNit().equals(input.getNit()))) {
            throw new RuntimeException("Ya existe un proveedor con ese NIT.");
        }

        if (proveedorRepository.findAll().stream().anyMatch(p -> p.getEmail().equals(input.getEmail()))) {
            throw new RuntimeException("Ya existe un proveedor con ese email.");
        }

        // Crear y guardar proveedor
        Proveedor proveedor = new Proveedor();
        proveedor.setNombre(input.getNombre());
        proveedor.setNit(input.getNit());
        proveedor.setDireccion(input.getDireccion());
        proveedor.setTelefono(input.getTelefono());
        proveedor.setEmail(input.getEmail());
        proveedor.setFechaRegistro(LocalDateTime.now());
        proveedor.setActivo(true);
        proveedor.setIdUsuarioCreador(input.getIdUsuarioCreador());

        Proveedor proveedorGuardado = proveedorRepository.save(proveedor);

        // Asociar productos
        if (input.getProductos() != null && !input.getProductos().isEmpty()) {
            List<Producto> productos = input.getProductos().stream().map(productoInput -> {
                Producto producto = new Producto();
                producto.setCodigo(productoInput.getCodigo());
                producto.setNombre(productoInput.getNombre());
                producto.setDescripcion(productoInput.getDescripcion());
                producto.setPrecio(productoInput.getPrecio());
                producto.setProveedor(proveedorGuardado);
                return producto;
            }).collect(Collectors.toList());

            productoRepository.saveAll(productos);
        }

        // Asociar condiciones de pago
        if (input.getCondicionesPago() != null && !input.getCondicionesPago().isEmpty()) {
            List<CondicionPago> condiciones = input.getCondicionesPago().stream().map(condInput -> {
                CondicionPago cond = new CondicionPago();
                cond.setProveedor(proveedorGuardado);
                cond.setDiasCredito(condInput.getDiasCredito());
                cond.setFechaInicio(condInput.getFechaInicio());
                cond.setFechaFin(condInput.getFechaFin());
                cond.setNota(condInput.getNota());
                return cond;
            }).collect(Collectors.toList());

            condicionPagoRepository.saveAll(condiciones);
        }

        return proveedorGuardado;
    }

    // Obtener todos los proveedores
    public List<Proveedor> getAll() {
        return proveedorRepository.findAll();
    }

    // Obtener proveedor por ID
    public Proveedor getById(Long id) {
        return proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con ID: " + id));
    }

    // Actualizar proveedor
    public Proveedor update(Long id, String nombre, String nit, String direccion, String telefono, String email, Boolean activo) {
        Proveedor proveedor = getById(id);

        if (nombre != null) proveedor.setNombre(nombre);
        if (nit != null) proveedor.setNit(nit);
        if (direccion != null) proveedor.setDireccion(direccion);
        if (telefono != null) proveedor.setTelefono(telefono);
        if (email != null) proveedor.setEmail(email);
        if (activo != null) proveedor.setActivo(activo);

        return proveedorRepository.save(proveedor);
    }

    // Borrado lógico
    public boolean delete(Long id) {
        Proveedor proveedor = getById(id);
        proveedor.setActivo(false);
        proveedorRepository.save(proveedor);
        return true;
    }
}
