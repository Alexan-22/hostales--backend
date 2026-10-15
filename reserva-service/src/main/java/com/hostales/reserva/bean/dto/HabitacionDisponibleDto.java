package com.hostales.reserva.bean.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HabitacionDisponibleDto {

    private Long habitacionId;
    private Long tipoHabitacionId;
    private String numero;
    private String tipo;
    private Short capacidad;
    private BigDecimal precioNoche;
    private long noches;
    private BigDecimal totalEstimado;
    private String moneda;
}