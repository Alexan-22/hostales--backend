package com.hostales.habitacion.bean.dto;

import lombok.*;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HabitacionResponseDto {

    private Long habitacionId;
    private Long empresaId;
    private Long hostalId;
    private Long tipoHabitacionId;
    private String numero;
    private Short piso;
    private String estadoOperativo;
    private OffsetDateTime creadoEn;
}