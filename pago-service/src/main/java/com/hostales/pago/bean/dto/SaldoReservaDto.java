package com.hostales.pago.bean.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SaldoReservaDto {

    private Long empresaId;
    private Long hostalId;
    private Long reservaId;

    private String moneda;

    private BigDecimal totalReserva;
    private BigDecimal totalPagado;
    private BigDecimal saldoPendiente;
}