package com.petmanager.provider_service.repository;

import com.petmanager.provider_service.model.CondicionPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CondicionPagoRepository extends JpaRepository<CondicionPago, Long> {
}
