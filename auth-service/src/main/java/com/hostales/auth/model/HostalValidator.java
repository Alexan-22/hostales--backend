package com.hostales.auth.model;

import com.hostales.auth.bean.dto.EmpresaResumenDto;
import com.hostales.auth.bean.dto.HostalResumenDto;
import com.hostales.auth.client.HostalClient;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class HostalValidator {

    private final HostalClient hostalClient;

    public EmpresaResumenDto validarEmpresaActiva(Long empresaId) {
        validarId(empresaId, "empresa");

        EmpresaResumenDto empresa;

        try {
            empresa = hostalClient.buscarEmpresa(empresaId);
        } catch (FeignException ex) {
            manejarErrorFeign(ex);
            return null;
        }

        if (empresa == null
                || !Objects.equals(empresa.getEmpresaId(), empresaId)) {
            throw error(
                    HttpStatus.BAD_GATEWAY,
                    "hostal-service devolvió una empresa inesperada"
            );
        }

        if (!Boolean.TRUE.equals(empresa.getEstado())) {
            throw error(
                    HttpStatus.CONFLICT,
                    "La empresa está inactiva"
            );
        }

        return empresa;
    }

    public HostalResumenDto validarHostalActivo(
            Long empresaId,
            Long hostalId
    ) {
        validarId(empresaId, "empresa");
        validarId(hostalId, "hostal");

        HostalResumenDto hostal;

        try {
            hostal = hostalClient.buscarHostal(
                    empresaId,
                    hostalId
            );
        } catch (FeignException ex) {
            manejarErrorFeign(ex);
            return null;
        }

        if (hostal == null
                || !Objects.equals(hostal.getEmpresaId(), empresaId)
                || !Objects.equals(hostal.getHostalId(), hostalId)) {
            throw error(
                    HttpStatus.BAD_GATEWAY,
                    "hostal-service devolvió un hostal inesperado"
            );
        }

        if (!Boolean.TRUE.equals(hostal.getEstado())) {
            throw error(
                    HttpStatus.CONFLICT,
                    "El hostal está inactivo"
            );
        }

        return hostal;
    }

    private void manejarErrorFeign(FeignException ex) {
        if (ex.status() == 404) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Empresa o hostal no encontrado",
                    ex
            );
        }

        if (ex.status() == 409) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La empresa o el hostal están inactivos",
                    ex
            );
        }

        throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "No se pudo consultar hostal-service",
                ex
        );
    }

    private void validarId(Long id, String recurso) {
        if (id == null || id <= 0) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "El ID de " + recurso + " debe ser positivo"
            );
        }
    }

    private ResponseStatusException error(
            HttpStatus estado,
            String mensaje
    ) {
        return new ResponseStatusException(estado, mensaje);
    }
}
