package com.hostales.auth.bean.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CambiarEstadoUsuarioDto {

    @NotNull(message = "El estado es obligatorio")
    private Boolean estado;
}