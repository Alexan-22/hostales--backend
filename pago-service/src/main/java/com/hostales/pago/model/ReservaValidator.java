package com.hostales.pago.model;

import com.hostales.pago.bean.dto.ReservaResumenDto;
import com.hostales.pago.client.ReservaClient;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ReservaValidator {

    private static final Set<String> ESTADOS_CONOCIDOS = Set.of(
            "CONFIRMADA",
            "EN_CURSO",
            "FINALIZADA",
            "CANCELADA",
            "NO_PRESENTADO"
    );

    private static final Set<String> ESTADOS_CON_PAGO = Set.of(
            "CONFIRMADA",
            "EN_CURSO",
            "FINALIZADA"
    );

    private static final BigDecimal TOTAL_MAXIMO =
            new BigDecimal("9999999999.99");

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
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Empresa, hostal o reserva no encontrados",
                        ex
                );
            }

            if (ex.status() == 409) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "La empresa, hostal o reserva no están disponibles",
                        ex
                );
            }

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo consultar reserva-service",
                    ex
            );
        }

        validarRespuesta(reserva, empresaId, hostalId, reservaId);
        return reserva;
    }

    public void validarPermitePago(ReservaResumenDto reserva) {
        if (reserva.getEstado() == null
                || !ESTADOS_CON_PAGO.contains(reserva.getEstado())) {
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

        BigDecimal total = reserva.getTotal();

        if (total == null
                || total.signum() < 0
                || total.compareTo(TOTAL_MAXIMO) > 0
                || total.stripTrailingZeros().scale() > 2
                || reserva.getMoneda() == null
                || !reserva.getMoneda().matches("[A-Z]{3}")
                || reserva.getEstado() == null
                || !ESTADOS_CONOCIDOS.contains(reserva.getEstado())) {
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
}