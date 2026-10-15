package com.hostales.auth.bean.entity;

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
        name = "usuarios",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_usuario_correo",
                        columnNames = "correo"
                ),
                @UniqueConstraint(
                        name = "uq_usuario_empresa",
                        columnNames = {
                                "empresa_id",
                                "usuario_id"
                        }
                )
        }
)
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rol_id", nullable = false)
    @ToString.Exclude
    private Rol rol;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "apellido", nullable = false, length = 100)
    private String apellido;

    @Column(
            name = "correo",
            nullable = false,
            unique = true,
            length = 150
    )
    private String correo;

    @Column(
            name = "password_hash",
            nullable = false,
            length = 255
    )
    @ToString.Exclude
    private String passwordHash;

    @Builder.Default
    @Column(name = "estado", nullable = false)
    private Boolean estado = true;

    @Column(
            name = "creado_en",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime creadoEn;

    @PrePersist
    private void antesDeGuardar() {
        if (creadoEn == null) {
            creadoEn = OffsetDateTime.now();
        }

        if (estado == null) {
            estado = true;
        }

        if (correo != null) {
            correo = correo.trim().toLowerCase();
        }
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }

        if (!(objeto instanceof Usuario otro)) {
            return false;
        }

        return getUsuarioId() != null
                && getUsuarioId().equals(otro.getUsuarioId());
    }

    @Override
    public int hashCode() {
        return Usuario.class.hashCode();
    }
}