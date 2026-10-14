package com.hostales.reserva.model;

import com.hostales.reserva.bean.dto.EmpresaResumenDto;
import com.hostales.reserva.client.EmpresaClient;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class EmpresaValidator {

    private final EmpresaClient empresaClient;

    public void validarActiva(Long empresaId) {
        if (empresaId == null || empresaId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "El ID de empresa debe ser positivo"
            );
        }

        EmpresaResumenDto empresa;

        try {
            empresa = empresaClient.buscarEmpresa(empresaId);
        } catch (FeignException ex) {
            if (ex.status() == 404) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Empresa no encontrada"
                );
            }

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo consultar el servicio de empresas",
                    ex
            );
        }

        if (empresa == null || !Objects.equals(empresa.getEmpresaId(), empresaId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY, "El servicio devolvió una empresa inesperada"
            );
        }

        if (!Boolean.TRUE.equals(empresa.getEstado())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "La empresa está inactiva"
            );
        }
    }
}