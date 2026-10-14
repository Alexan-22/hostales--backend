package com.hostales.reserva.bean.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HuespedDto {

    @NotBlank
    @Pattern(
            regexp = "DNI|CE|PASAPORTE",
            message = "El tipo de documento debe ser DNI, CE o PASAPORTE"
    )
    private String tipoDocumento;

    @NotBlank
    @Size(max = 30)
    private String numeroDocumento;

    @NotBlank
    @Size(max = 100)
    private String nombre;

    @NotBlank
    @Size(max = 100)
    private String apellido;

    @Email
    @Size(max = 150)
    private String correo;

    @Size(max = 20)
    private String telefono;
}
