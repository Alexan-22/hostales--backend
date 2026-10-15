package com.hostales.pago.client;

import com.hostales.pago.bean.dto.ReservaResumenDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "reserva-service",
        contextId = "reservaClient"
)
public interface ReservaClient {

    @GetMapping(
            "/api/empresas/{empresaId}"
                    + "/hostales/{hostalId}"
                    + "/reservas/{reservaId}"
    )
    ReservaResumenDto buscarReserva(
            @PathVariable("empresaId") Long empresaId,
            @PathVariable("hostalId") Long hostalId,
            @PathVariable("reservaId") Long reservaId
    );
}
