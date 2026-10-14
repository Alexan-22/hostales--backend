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
public class HostalResumenDto {

    private Long hostalId;
    private Long empresaId;
    private String nombre;
    private String zonaHoraria;
    private String moneda;
    private Boolean estado;
}
