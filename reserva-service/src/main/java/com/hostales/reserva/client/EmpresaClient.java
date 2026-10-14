package com.hostales.reserva.client;

import com.hostales.reserva.bean.dto.EmpresaResumenDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "hostal-service", contextId = "empresaClient")
public interface EmpresaClient {

    @GetMapping("/api/empresas/{empresaId}")
    EmpresaResumenDto buscarEmpresa(@PathVariable("empresaId") Long empresaId);
}