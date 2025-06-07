package com.petmanager.supplier_service.repository;

import com.petmanager.supplier_service.model.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
    // Aquí se pueden agregar métodos personalizados más adelante
}
