package com.hostales.reserva.bean.dto;

import lombok.*;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HuespedResponseDto {

    private Long huespedId;
    private Long empresaId;
    private String tipoDocumento;
    private String numeroDocumento;
    private String nombre;
    private String apellido;
    private String correo;
    private String telefono;
    private Boolean estado;
    private OffsetDateTime creadoEn;
}
