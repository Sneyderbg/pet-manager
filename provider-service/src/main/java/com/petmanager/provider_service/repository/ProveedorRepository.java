package com.petmanager.provider_service.repository;

import com.petmanager.provider_service.model.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
    // Aquí podrías agregar métodos personalizados si los necesitas más adelante
}
