package com.hostales.hostal.bean.dto;

import lombok.*;
import java.time.LocalTime;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HostalResponseDto {

    private Long hostalId;
    private Long empresaId;
    private String nombre;
    private String direccion;
    private String departamento;
    private String provincia;
    private String distrito;
    private String telefono;
    private String correo;
    private LocalTime horaEntrada;
    private LocalTime horaSalida;
    private String zonaHoraria;
    private String moneda;
    private Boolean estado;
    private OffsetDateTime creadoEn;
}