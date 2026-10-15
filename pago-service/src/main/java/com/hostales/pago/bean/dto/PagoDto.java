package com.hostales.pago.bean.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PagoDto {

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor que cero")
    @Digits(integer = 10, fraction = 2,
            message = "El monto admite hasta 10 enteros y 2 decimales")
    private BigDecimal monto;

    @NotBlank(message = "El método de pago es obligatorio")
    @Pattern(
            regexp = "EFECTIVO|YAPE|PLIN|TARJETA|TRANSFERENCIA",
            message = "Método de pago no permitido"
    )
    private String metodo;

    @Size(max = 100, message = "La referencia admite hasta 100 caracteres")
    private String referencia;

    @Size(max = 500, message = "Las observaciones admiten hasta 500 caracteres")
    private String observaciones;

    @NotNull(message = "La clave de operación es obligatoria")
    private UUID claveOperacion;
}
