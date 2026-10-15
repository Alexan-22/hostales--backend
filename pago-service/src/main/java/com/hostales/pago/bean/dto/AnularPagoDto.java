package com.hostales.pago.bean.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnularPagoDto {

    @NotBlank(message = "El motivo de anulación es obligatorio")
    @Size(
            max = 500,
            message = "El motivo admite hasta 500 caracteres"
    )
    private String motivo;
}
