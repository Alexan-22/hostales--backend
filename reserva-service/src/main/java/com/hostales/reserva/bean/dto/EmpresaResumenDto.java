package com.hostales.reserva.bean.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmpresaResumenDto {

    private Long empresaId;
    private String nombre;
    private Boolean estado;
}
