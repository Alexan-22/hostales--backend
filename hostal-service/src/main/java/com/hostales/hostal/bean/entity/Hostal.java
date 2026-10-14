package com.hostales.hostal.bean.entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.LocalTime;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
        name = "hostales",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_hostal_empresa_nombre",
                columnNames = {"empresa_id", "nombre"}
        )
)
public class Hostal implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hostal_id")
    private Long hostalId;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false, length = 250)
    private String direccion;

    @Column(nullable = false, length = 100)
    private String departamento;

    @Column(nullable = false, length = 100)
    private String provincia;

    @Column(nullable = false, length = 100)
    private String distrito;

    @Column(length = 20)
    private String telefono;

    @Column(length = 150)
    private String correo;

    @Builder.Default
    @Column(name = "hora_entrada", nullable = false)
    private LocalTime horaEntrada = LocalTime.of(14, 0);

    @Builder.Default
    @Column(name = "hora_salida", nullable = false)
    private LocalTime horaSalida = LocalTime.of(12, 0);

    @Builder.Default
    @Column(name = "zona_horaria", nullable = false, length = 60)
    private String zonaHoraria = "America/Lima";

    @Builder.Default
    @Column(nullable = false, length = 3)
    private String moneda = "PEN";

    @Builder.Default
    @Column(nullable = false)
    private Boolean estado = true;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn;

    @PrePersist
    private void antesDeGuardar() {
        creadoEn = OffsetDateTime.now();
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) return true;
        if (!(objeto instanceof Hostal otro)) return false;
        return getHostalId() != null && getHostalId().equals(otro.getHostalId());
    }

    @Override
    public int hashCode() {
        return Hostal.class.hashCode();
    }
}