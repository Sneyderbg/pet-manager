package com.petmanager.supplier_service.service;

import com.petmanager.supplier_service.exception.*;
import com.petmanager.supplier_service.model.Producto;
import com.petmanager.supplier_service.model.Proveedor;
import com.petmanager.supplier_service.repository.ProductoRepository;
import com.petmanager.supplier_service.repository.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    // Agregar producto a un proveedor existente
    public Producto agregarProducto(Long idProveedor, Producto producto) {
        // Validaciones de entrada
        if (idProveedor == null || idProveedor <= 0) {
            throw new InvalidDataException("El ID del proveedor debe ser un número positivo");
        }

        if (producto == null) {
            throw new InvalidDataException("Los datos del producto no pueden estar vacíos");
        }

        // Validar campos obligatorios
        validarCamposObligatoriosProducto(producto);

        // Verificar que el proveedor existe
        Proveedor proveedor = proveedorRepository.findById(idProveedor)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado con ID: " + idProveedor));

        // Verificar que el proveedor está activo
        if (!proveedor.getActivo()) {
            throw new BusinessValidationException("No se pueden agregar productos a un proveedor inactivo");
        }

        // Verificar código único
        if (productoRepository.existsByCodigo(producto.getCodigo().trim())) {
            throw new DuplicateResourceException("Ya existe un producto con el código: " + producto.getCodigo());
        }

        // Validar precio
        if (producto.getPrecio() != null && producto.getPrecio() < 0) {
            throw new InvalidDataException("El precio del producto no puede ser negativo");
        }



        // Limpiar y establecer datos
        producto.setCodigo(producto.getCodigo().trim().toUpperCase());
        producto.setNombre(producto.getNombre().trim());
        producto.setDescripcion(StringUtils.hasText(producto.getDescripcion()) ?
                producto.getDescripcion().trim() : "");
        producto.setProveedor(proveedor);

        return productoRepository.save(producto);
    }

    // Actualizar un producto existente
    public Producto actualizarProducto(Long idProducto, Producto productoActualizado) {
        // Validaciones de entrada
        if (idProducto == null || idProducto <= 0) {
            throw new InvalidDataException("El ID del producto debe ser un número positivo");
        }

        if (productoActualizado == null) {
            throw new InvalidDataException("Los datos del producto no pueden estar vacíos");
        }

        // Buscar producto existente
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + idProducto));

        // Verificar que el proveedor del producto esté activo
        if (!producto.getProveedor().getActivo()) {
            throw new BusinessValidationException("No se pueden modificar productos de un proveedor inactivo");
        }

        // Actualizar campos si están presentes
        if (StringUtils.hasText(productoActualizado.getNombre())) {
            if (productoActualizado.getNombre().trim().length() > 100) {
                throw new InvalidDataException("El nombre del producto no puede tener más de 100 caracteres");
            }
            producto.setNombre(productoActualizado.getNombre().trim());
        }

        if (StringUtils.hasText(productoActualizado.getCodigo())) {
            String nuevoCodigo = productoActualizado.getCodigo().trim().toUpperCase();

            // Validar formato del código
            if (!esCodigoValido(nuevoCodigo)) {
                throw new InvalidDataException("El código debe contener solo letras, números y guiones. Máximo 50 caracteres");
            }

            // Verificar que el nuevo código no esté en uso por otro producto
            if (!producto.getCodigo().equals(nuevoCodigo) &&
                    productoRepository.existsByCodigo(nuevoCodigo)) {
                throw new DuplicateResourceException("Ya existe otro producto con el código: " + nuevoCodigo);
            }
            producto.setCodigo(nuevoCodigo);
        }

        if (productoActualizado.getDescripcion() != null) {
            String descripcion = StringUtils.hasText(productoActualizado.getDescripcion()) ?
                    productoActualizado.getDescripcion().trim() : "";

            if (descripcion.length() > 255) {
                throw new InvalidDataException("La descripción no puede tener más de 255 caracteres");
            }
            producto.setDescripcion(descripcion);
        }

        if (productoActualizado.getPrecio() != null) {
            if (productoActualizado.getPrecio() < 0) {
                throw new InvalidDataException("El precio del producto no puede ser negativo");
            }



            producto.setPrecio(productoActualizado.getPrecio());
        }

        return productoRepository.save(producto);
    }

    // Listar productos por proveedor
    public List<Producto> listarPorProveedor(Long idProveedor) {
        // Validación de entrada
        if (idProveedor == null || idProveedor <= 0) {
            throw new InvalidDataException("El ID del proveedor debe ser un número positivo");
        }

        // Verificar que el proveedor existe
        if (!proveedorRepository.existsById(idProveedor)) {
            throw new ResourceNotFoundException("Proveedor no encontrado con ID: " + idProveedor);
        }

        List<Producto> productos = productoRepository.findAll().stream()
                .filter(p -> p.getProveedor().getIdProveedor().equals(idProveedor))
                .toList();

        if (productos.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron productos para el proveedor con ID: " + idProveedor);
        }

        return productos;
    }

    // Obtener producto por ID
    public Producto obtenerPorId(Long idProducto) {
        if (idProducto == null || idProducto <= 0) {
            throw new InvalidDataException("El ID del producto debe ser un número positivo");
        }

        return productoRepository.findById(idProducto)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + idProducto));
    }

    // Eliminar producto
    public boolean eliminarProducto(Long idProducto) {
        if (idProducto == null || idProducto <= 0) {
            throw new InvalidDataException("El ID del producto debe ser un número positivo");
        }

        Producto producto = obtenerPorId(idProducto);

        // Verificar que el proveedor esté activo
        if (!producto.getProveedor().getActivo()) {
            throw new BusinessValidationException("No se pueden eliminar productos de un proveedor inactivo");
        }

        productoRepository.deleteById(idProducto);
        return true;
    }

    // Buscar productos por código (puede devolver vacío)
    public List<Producto> buscarPorCodigo(String codigo) {
        if (!StringUtils.hasText(codigo)) {
            throw new InvalidDataException("El código de búsqueda no puede estar vacío");
        }

        String codigoLimpio = codigo.trim().toUpperCase();

        List<Producto> productos = productoRepository.findAll().stream()
                .filter(p -> p.getCodigo().contains(codigoLimpio))
                .toList();

        if (productos.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron productos con código que contenga: " + codigo);
        }

        return productos;
    }

    // Buscar productos por nombre (puede devolver vacío)
    public List<Producto> buscarPorNombre(String nombre) {
        if (!StringUtils.hasText(nombre)) {
            throw new InvalidDataException("El nombre de búsqueda no puede estar vacío");
        }

        String nombreLimpio = nombre.trim().toLowerCase();

        List<Producto> productos = productoRepository.findAll().stream()
                .filter(p -> p.getNombre().toLowerCase().contains(nombreLimpio))
                .toList();

        if (productos.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron productos con nombre que contenga: " + nombre);
        }

        return productos;
    }

    // ==============================================
    // MÉTODOS PRIVADOS DE VALIDACIÓN
    // ==============================================

    private void validarCamposObligatoriosProducto(Producto producto) {
        if (!StringUtils.hasText(producto.getCodigo())) {
            throw new InvalidDataException("El código del producto es obligatorio");
        }

        if (!StringUtils.hasText(producto.getNombre())) {
            throw new InvalidDataException("El nombre del producto es obligatorio");
        }

        if (producto.getPrecio() == null) {
            throw new InvalidDataException("El precio del producto es obligatorio");
        }

        // Validar longitud del código
        if (producto.getCodigo().trim().length() > 50) {
            throw new InvalidDataException("El código del producto no puede tener más de 50 caracteres");
        }

        // Validar longitud del nombre
        if (producto.getNombre().trim().length() > 100) {
            throw new InvalidDataException("El nombre del producto no puede tener más de 100 caracteres");
        }

        // Validar formato del código
        if (!esCodigoValido(producto.getCodigo().trim())) {
            throw new InvalidDataException("El código debe contener solo letras, números y guiones");
        }
    }

    private boolean esCodigoValido(String codigo) {
        return StringUtils.hasText(codigo) &&
                codigo.matches("^[A-Za-z0-9-]+$") &&
                codigo.length() >= 2 &&
                codigo.length() <= 50;
    }
}