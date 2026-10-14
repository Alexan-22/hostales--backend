package com.hostales.habitacion.model;

import com.hostales.habitacion.client.HostalClient;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import java.util.Objects;
import com.hostales.habitacion.bean.dto.HostalResumenDto;

@Component
@RequiredArgsConstructor
public class HostalValidator {

    private final HostalClient hostalClient;

    public void validarActivo(Long empresaId, Long hostalId) {
        HostalResumenDto hostal;

        try {
            hostal = hostalClient.buscarHostal(empresaId, hostalId);
        } catch (FeignException ex) {
            if (ex.status() == 404) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Empresa o hostal no encontrado"
                );
            }
            if (ex.status() == 409) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "La empresa está inactiva"
                );
            }
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo consultar el servicio de hostales"
            );
        }

        if (hostal == null
                || !Objects.equals(hostal.getEmpresaId(), empresaId)
                || !Objects.equals(hostal.getHostalId(), hostalId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY, "El servicio devolvió un hostal inesperado"
            );
        }

        if (!Boolean.TRUE.equals(hostal.getEstado())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "El hostal está inactivo"
            );
        }
    }
}
