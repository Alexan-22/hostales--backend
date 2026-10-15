package com.hostales.auth.bean.dto;

import com.hostales.auth.bean.enums.NombreRol;
import lombok.*;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponseDto {

    private String accessToken;

    @Builder.Default
    private String tokenType = "Bearer";

    private Long expiresIn;

    private Long usuarioId;
    private Long empresaId;

    private String nombreCompleto;
    private String correo;

    private NombreRol rol;
    private Set<Long> hostalesPermitidos;
}
