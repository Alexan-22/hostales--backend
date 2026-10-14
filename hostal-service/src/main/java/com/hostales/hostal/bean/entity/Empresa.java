package com.hostales.hostal.bean.entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "empresas")
public class Empresa implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "empresa_id")
    private Long empresaId;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(unique = true, length = 11)
    private String ruc;

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
        if (!(objeto instanceof Empresa otra)) return false;
        return getEmpresaId() != null && getEmpresaId().equals(otra.getEmpresaId());
    }

    @Override
    public int hashCode() {
        return Empresa.class.hashCode();
    }
}