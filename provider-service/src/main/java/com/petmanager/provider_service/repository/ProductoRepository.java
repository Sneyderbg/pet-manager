package com.petmanager.provider_service.repository;

import com.petmanager.provider_service.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    boolean existsByCodigo(String codigo); //  Agregado para evitar productos duplicados por código
}
