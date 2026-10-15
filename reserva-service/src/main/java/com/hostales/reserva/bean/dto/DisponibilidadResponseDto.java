package com.hostales.reserva.bean.dto;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DisponibilidadResponseDto {

    private Long empresaId;
    private Long hostalId;
    private Long habitacionId;
    private OffsetDateTime entrada;
    private OffsetDateTime salida;
    private boolean disponible;
    private String motivo;
}