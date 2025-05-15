package com.petmanager.provider_service.service;

import com.petmanager.provider_service.model.Producto;
import com.petmanager.provider_service.model.Proveedor;
import com.petmanager.provider_service.repository.ProductoRepository;
import com.petmanager.provider_service.repository.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    // Agregar producto a un proveedor existente
    public Producto agregarProducto(Long idProveedor, Producto producto) {
        if (productoRepository.existsByCodigo(producto.getCodigo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe un producto con el código: " + producto.getCodigo());
        }

        Proveedor proveedor = proveedorRepository.findById(idProveedor)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proveedor no encontrado con ID: " + idProveedor));

        producto.setProveedor(proveedor);

        return productoRepository.save(producto);
    }

    // Actualizar un producto existente
    public Producto actualizarProducto(Long idProducto, Producto productoActualizado) {
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado con ID: " + idProducto));

        if (productoActualizado.getNombre() != null)
            producto.setNombre(productoActualizado.getNombre());

        if (productoActualizado.getCodigo() != null)
            producto.setCodigo(productoActualizado.getCodigo());

        if (productoActualizado.getDescripcion() != null)
            producto.setDescripcion(productoActualizado.getDescripcion());

        if (productoActualizado.getPrecio() != null)
            producto.setPrecio(productoActualizado.getPrecio());

        return productoRepository.save(producto);
    }

    // Listar productos por proveedor
    public List<Producto> listarPorProveedor(Long idProveedor) {
        return productoRepository.findAll().stream()
                .filter(p -> p.getProveedor().getId().equals(idProveedor))
                .toList();
    }
}
