package com.hostales.reserva.client;

import com.hostales.reserva.bean.dto.HostalResumenDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "hostal-service",
        contextId = "hostalClient"
)
public interface HostalClient {

    @GetMapping("/api/empresas/{empresaId}/hostales/{hostalId}")
    HostalResumenDto buscarHostal(
            @PathVariable("empresaId") Long empresaId,
            @PathVariable("hostalId") Long hostalId
    );
}
