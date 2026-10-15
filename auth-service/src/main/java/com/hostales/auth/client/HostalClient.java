package com.hostales.auth.client;

import com.hostales.auth.bean.dto.EmpresaResumenDto;
import com.hostales.auth.bean.dto.HostalResumenDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "hostal-service",
        contextId = "hostalClient"
)
public interface HostalClient {

    @GetMapping("/api/empresas/{empresaId}")
    EmpresaResumenDto buscarEmpresa(
            @PathVariable("empresaId") Long empresaId
    );

    @GetMapping(
            "/api/empresas/{empresaId}/hostales/{hostalId}"
    )
    HostalResumenDto buscarHostal(
            @PathVariable("empresaId") Long empresaId,
            @PathVariable("hostalId") Long hostalId
    );
}