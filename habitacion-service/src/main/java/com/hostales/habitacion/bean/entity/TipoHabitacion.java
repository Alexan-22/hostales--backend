package com.hostales.habitacion.bean.entity;

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
@Table(name = "tipos_habitacion")
public class TipoHabitacion implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tipo_habitacion_id")
    private Long tipoHabitacionId;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "hostal_id", nullable = false)
    private Long hostalId;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false)
    private Short capacidad;

    @Column(name = "precio_noche", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioNoche;

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
        if (!(objeto instanceof TipoHabitacion otro)) return false;
        return getTipoHabitacionId() != null
                && getTipoHabitacionId().equals(otro.getTipoHabitacionId());
    }

    @Override
    public int hashCode() {
        return TipoHabitacion.class.hashCode();
    }
}
