package com.hostales.reserva.client;

import com.hostales.reserva.bean.dto.HabitacionResumenDto;
import com.hostales.reserva.bean.dto.TipoHabitacionResumenDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "habitacion-service",
        contextId = "habitacionClient"
)
public interface HabitacionClient {

    @GetMapping(
            "/api/empresas/{empresaId}/hostales/{hostalId}/habitaciones/{habitacionId}"
    )
    HabitacionResumenDto buscarHabitacion(
            @PathVariable("empresaId") Long empresaId,
            @PathVariable("hostalId") Long hostalId,
            @PathVariable("habitacionId") Long habitacionId
    );

    @GetMapping(
            "/api/empresas/{empresaId}/hostales/{hostalId}/tipos-habitacion/{tipoHabitacionId}"
    )
    TipoHabitacionResumenDto buscarTipoHabitacion(
            @PathVariable("empresaId") Long empresaId,
            @PathVariable("hostalId") Long hostalId,
            @PathVariable("tipoHabitacionId") Long tipoHabitacionId
    );
}
