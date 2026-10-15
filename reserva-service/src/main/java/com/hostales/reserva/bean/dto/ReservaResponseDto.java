package com.hostales.reserva.bean.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReservaResponseDto {

    private Long reservaId;
    private Long empresaId;
    private Long hostalId;
    private Long habitacionId;
    private Long huespedId;
    private OffsetDateTime entrada;
    private OffsetDateTime salida;
    private Short cantidadPersonas;
    private BigDecimal total;
    private String moneda;
    private String estado;
    private String origen;
    private String observaciones;
    private OffsetDateTime creadoEn;
    private OffsetDateTime entradaReal;
    private OffsetDateTime salidaReal;
}