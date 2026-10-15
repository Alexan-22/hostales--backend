package com.hostales.auth.bean.dto;

import com.hostales.auth.bean.enums.NombreRol;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UsuarioResponseDto {

    private Long usuarioId;
    private Long empresaId;

    private String nombre;
    private String apellido;
    private String correo;

    private NombreRol rol;
    private Boolean estado;

    private Set<Long> hostalesIds;

    private OffsetDateTime creadoEn;
}