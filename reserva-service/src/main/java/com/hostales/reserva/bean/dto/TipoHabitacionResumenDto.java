package com.hostales.reserva.bean.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TipoHabitacionResumenDto {

    private Long tipoHabitacionId;
    private Long empresaId;
    private Long hostalId;
    private String nombre;
    private Short capacidad;
    private BigDecimal precioNoche;
    private Boolean estado;
}