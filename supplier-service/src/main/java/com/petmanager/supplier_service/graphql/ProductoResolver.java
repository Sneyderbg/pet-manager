package com.petmanager.supplier_service.graphql;

import com.petmanager.supplier_service.model.Producto;
import com.petmanager.supplier_service.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class ProductoResolver {

    @Autowired
    private ProductoService productoService;

    @MutationMapping
    public Producto agregarProducto(
            @Argument Long idProveedor,
            @Argument String codigo,
            @Argument String nombre,
            @Argument String descripcion,
            @Argument Double precio
    ) {
        Producto producto = new Producto();
        producto.setCodigo(codigo);
        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setPrecio(precio);

        // ✅ Cambiado para enviar el objeto producto
        return productoService.agregarProducto(idProveedor, producto);
    }

    @MutationMapping
    public Producto actualizarProducto(
            @Argument Long idProducto,
            @Argument String codigo,
            @Argument String nombre,
            @Argument String descripcion,
            @Argument Double precio
    ) {
        Producto producto = new Producto();
        producto.setCodigo(codigo);
        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setPrecio(precio);

        return productoService.actualizarProducto(idProducto, producto);
    }

    @QueryMapping
    public List<Producto> listarProductosPorProveedor(@Argument Long idProveedor) {
        return productoService.listarPorProveedor(idProveedor);
    }
}
