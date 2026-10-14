package com.hostales.habitacion.bean.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HabitacionDto {

    @NotNull
    @Positive
    private Long tipoHabitacionId;

    @NotBlank
    @Size(max = 10)
    private String numero;

    @Builder.Default
    @NotNull
    private Short piso = 1;

    @Builder.Default
    @NotBlank
    @Pattern(
            regexp = "HABILITADA|MANTENIMIENTO|INACTIVA",
            message = "El estado debe ser HABILITADA, MANTENIMIENTO o INACTIVA"
    )
    private String estadoOperativo = "HABILITADA";
}
