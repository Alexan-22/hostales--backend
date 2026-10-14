package com.hostales.reserva.bean.entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
        name = "huespedes",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_huesped_documento",
                columnNames = {"empresa_id", "tipo_documento", "numero_documento"}
        )
)
public class Huesped implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "huesped_id")
    private Long huespedId;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "tipo_documento", nullable = false, length = 20)
    private String tipoDocumento;

    @Column(name = "numero_documento", nullable = false, length = 30)
    private String numeroDocumento;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(length = 150)
    private String correo;

    @Column(length = 20)
    private String telefono;

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
        if (!(objeto instanceof Huesped otro)) return false;
        return getHuespedId() != null && getHuespedId().equals(otro.getHuespedId());
    }

    @Override
    public int hashCode() {
        return Huesped.class.hashCode();
    }
}