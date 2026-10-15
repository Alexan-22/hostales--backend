package com.hostales.auth.bean.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class HostalResumenDto {

    private Long hostalId;
    private Long empresaId;
    private String nombre;
    private Boolean estado;
}
