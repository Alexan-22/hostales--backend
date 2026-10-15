package com.hostales.auth.bean.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@IdClass(UsuarioHostalId.class)
@Table(name = "usuarios_hostales")
public class UsuarioHostal implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Id
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Id
    @Column(name = "hostal_id", nullable = false)
    private Long hostalId;
}
