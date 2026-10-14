package com.hostales.reserva.bean.dto;

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
public class HabitacionResumenDto {

    private Long habitacionId;
    private Long empresaId;
    private Long hostalId;
    private Long tipoHabitacionId;
    private String numero;
    private String estadoOperativo;
}
