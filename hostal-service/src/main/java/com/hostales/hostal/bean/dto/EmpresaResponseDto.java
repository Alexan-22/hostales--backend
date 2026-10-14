package com.hostales.hostal.bean.dto;

import lombok.*;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmpresaResponseDto {

    private Long empresaId;
    private String nombre;
    private String ruc;
    private String correo;
    private String telefono;
    private Boolean estado;
    private OffsetDateTime creadoEn;
}