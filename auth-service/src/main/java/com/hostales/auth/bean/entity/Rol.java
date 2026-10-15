package com.hostales.auth.bean.entity;

import com.hostales.auth.bean.enums.NombreRol;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "roles")
public class Rol implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rol_id")
    private Long rolId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "nombre",
            nullable = false,
            unique = true,
            length = 40
    )
    private NombreRol nombre;

    @Column(name = "descripcion", length = 200)
    private String descripcion;

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }

        if (!(objeto instanceof Rol otro)) {
            return false;
        }

        return getRolId() != null
                && getRolId().equals(otro.getRolId());
    }

    @Override
    public int hashCode() {
        return Rol.class.hashCode();
    }
}
