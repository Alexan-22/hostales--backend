package com.hostales.reserva.bean.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReservaDto {

    @NotNull
    @Positive
    private Long habitacionId;

    @NotNull
    @Positive
    private Long huespedId;

    @NotNull
    private OffsetDateTime entrada;

    @NotNull
    private OffsetDateTime salida;

    @NotNull
    @Min(1)
    private Short cantidadPersonas;

    @Builder.Default
    @NotBlank
    @Pattern(
            regexp = "RECEPCION|WEB",
            message = "El origen debe ser RECEPCION o WEB"
    )
    private String origen = "RECEPCION";

    @Size(max = 500)
    private String observaciones;
}
