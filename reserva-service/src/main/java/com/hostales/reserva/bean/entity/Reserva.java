package com.hostales.reserva.bean.entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "reservas")
public class Reserva implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reserva_id")
    private Long reservaId;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "hostal_id", nullable = false)
    private Long hostalId;

    @Column(name = "habitacion_id", nullable = false)
    private Long habitacionId;

    @Column(name = "huesped_id", nullable = false)
    private Long huespedId;

    @Column(nullable = false)
    private OffsetDateTime entrada;

    @Column(nullable = false)
    private OffsetDateTime salida;

    @Column(name = "cantidad_personas", nullable = false)
    private Short cantidadPersonas;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Builder.Default
    @Column(nullable = false, length = 3)
    private String moneda = "PEN";

    @Builder.Default
    @Column(nullable = false, length = 20)
    private String estado = "CONFIRMADA";

    @Builder.Default
    @Column(nullable = false, length = 20)
    private String origen = "RECEPCION";

    @Column(length = 500)
    private String observaciones;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn;

    @PrePersist
    private void antesDeGuardar() {
        creadoEn = OffsetDateTime.now();
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) return true;
        if (!(objeto instanceof Reserva otra)) return false;
        return getReservaId() != null && getReservaId().equals(otra.getReservaId());
    }

    @Override
    public int hashCode() {
        return Reserva.class.hashCode();
    }
}