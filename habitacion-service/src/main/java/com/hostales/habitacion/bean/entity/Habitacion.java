package com.hostales.habitacion.bean.entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "habitaciones")
public class Habitacion implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "habitacion_id")
    private Long habitacionId;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "hostal_id", nullable = false)
    private Long hostalId;

    @Column(name = "tipo_habitacion_id", nullable = false)
    private Long tipoHabitacionId;

    @Column(nullable = false, length = 10)
    private String numero;

    @Builder.Default
    @Column(nullable = false)
    private Short piso = 1;

    @Builder.Default
    @Column(name = "estado_operativo", nullable = false, length = 20)
    private String estadoOperativo = "HABILITADA";

    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn;

    @PrePersist
    private void antesDeGuardar() {
        creadoEn = OffsetDateTime.now();
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) return true;
        if (!(objeto instanceof Habitacion otra)) return false;
        return getHabitacionId() != null
                && getHabitacionId().equals(otra.getHabitacionId());
    }

    @Override
    public int hashCode() {
        return Habitacion.class.hashCode();
    }
}
