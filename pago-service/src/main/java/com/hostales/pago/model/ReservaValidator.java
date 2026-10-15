package com.hostales.pago.model;

import com.hostales.pago.bean.dto.ReservaResumenDto;
import com.hostales.pago.client.ReservaClient;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ReservaValidator {

    private final ReservaClient reservaClient;

    public ReservaResumenDto consultar(
            Long empresaId,
            Long hostalId,
            Long reservaId
    ) {
        validarId(empresaId, "empresa");
        validarId(hostalId, "hostal");
        validarId(reservaId, "reserva");

        ReservaResumenDto reserva;

        try {
            reserva = reservaClient.buscarReserva(
                    empresaId,
                    hostalId,
                    reservaId
            );
        } catch (FeignException ex) {
            if (ex.status() == 404) {
                throw error(
                        HttpStatus.NOT_FOUND,
                        "Empresa, hostal o reserva no encontrados",
                        ex
                );
            }

            if (ex.status() == 409) {
                throw error(
                        HttpStatus.CONFLICT,
                        "La empresa, hostal o reserva no están disponibles",
                        ex
                );
            }

            throw error(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo consultar reserva-service",
                    ex
            );
        }

        validarRespuesta(reserva, empresaId, hostalId, reservaId);

        return reserva;
    }

    public void validarPermitePago(ReservaResumenDto reserva) {
        boolean permitido =
                "CONFIRMADA".equals(reserva.getEstado())
                        || "EN_CURSO".equals(reserva.getEstado())
                        || "FINALIZADA".equals(reserva.getEstado());

        if (!permitido) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El estado de la reserva no permite pagos"
            );
        }
    }

    private void validarRespuesta(
            ReservaResumenDto reserva,
            Long empresaId,
            Long hostalId,
            Long reservaId
    ) {
        if (reserva == null
                || !Objects.equals(reserva.getEmpresaId(), empresaId)
                || !Objects.equals(reserva.getHostalId(), hostalId)
                || !Objects.equals(reserva.getReservaId(), reservaId)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "reserva-service devolvió una reserva inesperada"
            );
        }

        if (reserva.getTotal() == null
                || reserva.getTotal().signum() <= 0
                || reserva.getMoneda() == null
                || !reserva.getMoneda().matches("^[A-Z]{3}$")
                || reserva.getEstado() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "La reserva contiene datos de cobro inválidos"
            );
        }
    }

    private void validarId(Long id, String recurso) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El ID de " + recurso + " debe ser positivo"
            );
        }
    }

    private ResponseStatusException error(
            HttpStatus estado,
            String mensaje,
            Throwable causa
    ) {
        return new ResponseStatusException(
                estado,
                mensaje,
                causa
        );
    }
}