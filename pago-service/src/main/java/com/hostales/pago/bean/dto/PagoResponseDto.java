package com.hostales.pago.bean.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PagoResponseDto {

    private Long pagoId;

    private Long empresaId;
    private Long hostalId;
    private Long reservaId;

    private BigDecimal monto;
    private String moneda;
    private String metodo;
    private String referencia;

    private String estado;
    private String observaciones;

    private OffsetDateTime pagadoEn;
    private OffsetDateTime creadoEn;

    private UUID claveOperacion;

    private OffsetDateTime anuladoEn;
    private String motivoAnulacion;
}