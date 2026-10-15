package com.hostales.auth.bean.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UsuarioHostalId implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long empresaId;
    private Long usuarioId;
    private Long hostalId;
}
