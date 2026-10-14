package com.hostales.hostal.bean.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HostalDto {

    @NotBlank
    @Size(max = 150)
    private String nombre;

    @NotBlank
    @Size(max = 250)
    private String direccion;

    @NotBlank
    @Size(max = 100)
    private String departamento;

    @NotBlank
    @Size(max = 100)
    private String provincia;

    @NotBlank
    @Size(max = 100)
    private String distrito;

    @Size(max = 20)
    private String telefono;

    @Email
    @Size(max = 150)
    private String correo;

    @Builder.Default
    @NotNull
    private LocalTime horaEntrada = LocalTime.of(14, 0);

    @Builder.Default
    @NotNull
    private LocalTime horaSalida = LocalTime.of(12, 0);

    @Builder.Default
    @NotBlank
    @Size(max = 60)
    private String zonaHoraria = "America/Lima";

    @Builder.Default
    @NotBlank
    @Pattern(regexp = "^[A-Z]{3}$")
    private String moneda = "PEN";
}