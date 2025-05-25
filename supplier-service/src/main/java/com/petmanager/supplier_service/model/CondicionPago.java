package com.petmanager.supplier_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "condicion_pago")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CondicionPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_condicion_pago")
    private Long id;

    @Column(name = "dias_credito")
    private Integer diasCredito;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "nota", columnDefinition = "text")
    private String nota;

    @ManyToOne
    @JoinColumn(name = "id_proveedor")
    private Proveedor proveedor;
}

