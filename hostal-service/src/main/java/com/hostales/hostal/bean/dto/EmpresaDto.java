package com.hostales.hostal.bean.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmpresaDto {

    @NotBlank
    @Size(max = 150)
    private String nombre;

    @Pattern(regexp = "^[0-9]{11}$", message = "El RUC debe tener 11 dígitos")
    private String ruc;

    @Email
    @Size(max = 150)
    private String correo;

    @Size(max = 20)
    private String telefono;
}