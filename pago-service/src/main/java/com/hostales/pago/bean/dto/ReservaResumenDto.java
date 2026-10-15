package com.hostales.pago.bean.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReservaResumenDto {

    private Long reservaId;
    private Long empresaId;
    private Long hostalId;

    private BigDecimal total;
    private String moneda;
    private String estado;
}
