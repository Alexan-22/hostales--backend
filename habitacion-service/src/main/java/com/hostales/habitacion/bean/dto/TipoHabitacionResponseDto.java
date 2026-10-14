package com.hostales.habitacion.bean.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TipoHabitacionResponseDto {

    private Long tipoHabitacionId;
    private Long empresaId;
    private Long hostalId;
    private String nombre;
    private String descripcion;
    private Short capacidad;
    private BigDecimal precioNoche;
    private Boolean estado;
    private OffsetDateTime creadoEn;
}