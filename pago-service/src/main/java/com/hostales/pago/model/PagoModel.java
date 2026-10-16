package com.hostales.pago.model;

import com.hostales.pago.bean.dto.AnularPagoDto;
import com.hostales.pago.bean.dto.PagoDto;
import com.hostales.pago.bean.dto.PagoResponseDto;
import com.hostales.pago.bean.dto.ReservaResumenDto;
import com.hostales.pago.bean.dto.SaldoReservaDto;
import com.hostales.pago.bean.entity.Pago;
import com.hostales.pago.persistence.PagoRepository;
import com.hostales.pago.usecase.PagoUseCase;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PagoModel implements PagoUseCase {

    private final PagoRepository pagoRepository;
    private final ReservaValidator reservaValidator;
    private final PagoBloqueo pagoBloqueo;
    private final Validator validator;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PagoResponseDto registrarPago(
            Long empresaId,
            Long hostalId,
            Long reservaId,
            PagoDto dto
    ) {
        validarIds(empresaId, hostalId, reservaId);
        validarDto(dto);

        String metodo = dto.getMetodo();
        String referencia = normalizar(dto.getReferencia());
        String observaciones = normalizar(dto.getObservaciones());

        validarReferencia(metodo, referencia);

        pagoBloqueo.bloquear(
                "pago:operacion:" + empresaId + ":" + dto.getClaveOperacion()
        );

        Pago existente = pagoRepository
                .findByEmpresaIdAndClaveOperacion(
                        empresaId,
                        dto.getClaveOperacion()
                )
                .orElse(null);

        if (existente != null) {
            validarIdempotencia(
                    existente,
                    hostalId,
                    reservaId,
                    dto,
                    referencia,
                    observaciones
            );

            return convertir(existente);
        }

        bloquearReserva(empresaId, hostalId, reservaId);

        ReservaResumenDto reserva = reservaValidator.consultar(
                empresaId,
                hostalId,
                reservaId
        );

        reservaValidator.validarPermitePago(reserva);

        BigDecimal totalPagado = pagoRepository.sumarPagosRegistrados(
                empresaId,
                hostalId,
                reservaId
        );

        BigDecimal saldo = reserva.getTotal().subtract(totalPagado);

        if (saldo.signum() <= 0) {
            throw error(
                    HttpStatus.CONFLICT,
                    "La reserva no tiene saldo pendiente"
            );
        }

        if (dto.getMonto().compareTo(saldo) > 0) {
            throw error(
                    HttpStatus.CONFLICT,
                    "El monto supera el saldo pendiente de "
                            + saldo.toPlainString() + " " + reserva.getMoneda()
            );
        }

        Pago pago = Pago.builder()
                .empresaId(empresaId)
                .hostalId(hostalId)
                .reservaId(reservaId)
                .monto(dto.getMonto())
                .moneda(reserva.getMoneda())
                .metodo(metodo)
                .referencia(referencia)
                .observaciones(observaciones)
                .claveOperacion(dto.getClaveOperacion())
                .estado("REGISTRADO")
                .build();

        return convertir(pagoRepository.saveAndFlush(pago));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponseDto> listarPagos(
            Long empresaId,
            Long hostalId,
            Long reservaId
    ) {
        validarIds(empresaId, hostalId, reservaId);

        reservaValidator.consultar(empresaId, hostalId, reservaId);

        return pagoRepository
                .findByEmpresaIdAndHostalIdAndReservaIdOrderByPagadoEnAscPagoIdAsc(
                        empresaId,
                        hostalId,
                        reservaId
                )
                .stream()
                .map(this::convertir)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SaldoReservaDto consultarSaldo(
            Long empresaId,
            Long hostalId,
            Long reservaId
    ) {
        validarIds(empresaId, hostalId, reservaId);

        ReservaResumenDto reserva = reservaValidator.consultar(
                empresaId,
                hostalId,
                reservaId
        );

        BigDecimal totalPagado = pagoRepository.sumarPagosRegistrados(
                empresaId,
                hostalId,
                reservaId
        );

        BigDecimal diferencia = reserva.getTotal().subtract(totalPagado);

        return SaldoReservaDto.builder()
                .empresaId(empresaId)
                .hostalId(hostalId)
                .reservaId(reservaId)
                .moneda(reserva.getMoneda())
                .totalReserva(reserva.getTotal())
                .totalPagado(totalPagado)
                .saldoPendiente(diferencia.max(BigDecimal.ZERO))
                .saldoAFavor(diferencia.negate().max(BigDecimal.ZERO))
                .build();
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PagoResponseDto anularPago(
            Long empresaId,
            Long hostalId,
            Long reservaId,
            Long pagoId,
            AnularPagoDto dto
    ) {
        validarIds(empresaId, hostalId, reservaId);

        if (pagoId == null || pagoId <= 0) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "El ID del pago debe ser positivo"
            );
        }

        validarDto(dto);
        String motivo = dto.getMotivo().strip();

        bloquearReserva(empresaId, hostalId, reservaId);

        Pago pago = pagoRepository
                .findByPagoIdAndEmpresaIdAndHostalIdAndReservaId(
                        pagoId,
                        empresaId,
                        hostalId,
                        reservaId
                )
                .orElseThrow(() -> error(
                        HttpStatus.NOT_FOUND,
                        "Pago no encontrado en esta reserva"
                ));

        if ("ANULADO".equals(pago.getEstado())) {
            if (!Objects.equals(pago.getMotivoAnulacion(), motivo)) {
                throw error(
                        HttpStatus.CONFLICT,
                        "El pago ya fue anulado con otro motivo"
                );
            }

            return convertir(pago);
        }

        if (!"REGISTRADO".equals(pago.getEstado())) {
            throw error(
                    HttpStatus.CONFLICT,
                    "El estado del pago no permite anulación"
            );
        }

        reservaValidator.consultar(empresaId, hostalId, reservaId);

        pago.setEstado("ANULADO");
        pago.setAnuladoEn(OffsetDateTime.now());
        pago.setMotivoAnulacion(motivo);

        return convertir(pagoRepository.saveAndFlush(pago));
    }

    private void bloquearReserva(
            Long empresaId,
            Long hostalId,
            Long reservaId
    ) {
        pagoBloqueo.bloquear(
                "pago:reserva:"
                        + empresaId + ":"
                        + hostalId + ":"
                        + reservaId
        );
    }

    private void validarIdempotencia(
            Pago existente,
            Long hostalId,
            Long reservaId,
            PagoDto dto,
            String referencia,
            String observaciones
    ) {
        boolean iguales =
                Objects.equals(existente.getHostalId(), hostalId)
                        && Objects.equals(existente.getReservaId(), reservaId)
                        && existente.getMonto().compareTo(dto.getMonto()) == 0
                        && Objects.equals(existente.getMetodo(), dto.getMetodo())
                        && Objects.equals(
                        normalizar(existente.getReferencia()),
                        referencia
                )
                        && Objects.equals(
                        normalizar(existente.getObservaciones()),
                        observaciones
                );

        if (!iguales) {
            throw error(
                    HttpStatus.CONFLICT,
                    "La clave de operación ya fue utilizada con otros datos"
            );
        }
    }

    private void validarReferencia(String metodo, String referencia) {
        if (!"EFECTIVO".equals(metodo) && referencia == null) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "La referencia es obligatoria para " + metodo
            );
        }
    }

    private <T> void validarDto(T dto) {
        if (dto == null) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "El cuerpo de la solicitud es obligatorio"
            );
        }

        Set<ConstraintViolation<T>> violaciones = validator.validate(dto);

        if (!violaciones.isEmpty()) {
            throw new ConstraintViolationException(violaciones);
        }
    }

    private void validarIds(
            Long empresaId,
            Long hostalId,
            Long reservaId
    ) {
        if (empresaId == null || empresaId <= 0
                || hostalId == null || hostalId <= 0
                || reservaId == null || reservaId <= 0) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "Los identificadores deben ser positivos"
            );
        }
    }

    private String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.strip();
    }

    private ResponseStatusException error(
            HttpStatus estado,
            String mensaje
    ) {
        return new ResponseStatusException(estado, mensaje);
    }

    private PagoResponseDto convertir(Pago pago) {
        return PagoResponseDto.builder()
                .pagoId(pago.getPagoId())
                .empresaId(pago.getEmpresaId())
                .hostalId(pago.getHostalId())
                .reservaId(pago.getReservaId())
                .monto(pago.getMonto())
                .moneda(pago.getMoneda())
                .metodo(pago.getMetodo())
                .referencia(pago.getReferencia())
                .estado(pago.getEstado())
                .observaciones(pago.getObservaciones())
                .pagadoEn(pago.getPagadoEn())
                .creadoEn(pago.getCreadoEn())
                .claveOperacion(pago.getClaveOperacion())
                .anuladoEn(pago.getAnuladoEn())
                .motivoAnulacion(pago.getMotivoAnulacion())
                .build();
    }
}