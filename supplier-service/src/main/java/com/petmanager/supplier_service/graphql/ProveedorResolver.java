package com.petmanager.supplier_service.graphql;

import com.petmanager.supplier_service.model.Proveedor;
import com.petmanager.supplier_service.service.ProveedorService;
import com.petmanager.supplier_service.dto.ProveedorInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class ProveedorResolver {

    @Autowired
    private ProveedorService proveedorService;

    @QueryMapping
    public List<Proveedor> getProveedores() {
        return proveedorService.getAll().stream()
                .peek(p -> p.setFechaRegistroString(
                        p.getFechaRegistro().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                ))
                .toList();
    }

    @QueryMapping
    public Proveedor getProveedorById(@Argument Long id) {
        Proveedor proveedor = proveedorService.getById(id);
        proveedor.setFechaRegistroString(
                proveedor.getFechaRegistro().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
        return proveedor;
    }

    @MutationMapping
    public Proveedor createProveedor(@Argument("input") ProveedorInput input) {
        Proveedor saved = proveedorService.crearProveedor(input);
        saved.setFechaRegistroString(
                saved.getFechaRegistro().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
        return saved;
    }

    @MutationMapping
    public Proveedor updateProveedor(
            @Argument Long id,
            @Argument String nombre,
            @Argument String nit,
            @Argument String direccion,
            @Argument String telefono,
            @Argument String email,
            @Argument Boolean activo
    ) {
        Proveedor updated = proveedorService.update(id, nombre, nit, direccion, telefono, email, activo);
        updated.setFechaRegistroString(updated.getFechaRegistro().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return updated;
    }

    @MutationMapping
    public Boolean deleteProveedor(@Argument Long id) {
        return proveedorService.delete(id);
    }
}
