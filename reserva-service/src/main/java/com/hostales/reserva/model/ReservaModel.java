package com.hostales.reserva.model;

import com.hostales.reserva.bean.dto.HabitacionResumenDto;
import com.hostales.reserva.bean.dto.HostalResumenDto;
import com.hostales.reserva.bean.dto.ReservaDto;
import com.hostales.reserva.bean.dto.ReservaResponseDto;
import com.hostales.reserva.bean.dto.TipoHabitacionResumenDto;
import com.hostales.reserva.bean.entity.Huesped;
import com.hostales.reserva.bean.entity.Reserva;
import com.hostales.reserva.client.HabitacionClient;
import com.hostales.reserva.client.HostalClient;
import com.hostales.reserva.persistence.HuespedRepository;
import com.hostales.reserva.persistence.ReservaRepository;
import com.hostales.reserva.usecase.ReservaUseCase;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class ReservaModel implements ReservaUseCase {

    private final ReservaRepository reservaRepository;
    private final HuespedRepository huespedRepository;
    private final EmpresaValidator empresaValidator;
    private final HostalClient hostalClient;
    private final HabitacionClient habitacionClient;

    @Override
    public ReservaResponseDto registrarReserva(
            Long empresaId, Long hostalId, ReservaDto dto
    ) {
        validarDatos(dto);
        HostalResumenDto hostal = validarHostal(empresaId, hostalId);

        Huesped huesped = huespedRepository
                .findByHuespedIdAndEmpresaId(dto.getHuespedId(), empresaId)
                .orElseThrow(() -> error(
                        HttpStatus.NOT_FOUND,
                        "Huésped no encontrado en esta empresa"
                ));

        if (!Boolean.TRUE.equals(huesped.getEstado())) {
            throw error(HttpStatus.CONFLICT, "El huésped está inactivo");
        }

        HabitacionResumenDto habitacion = consultar(
                () -> habitacionClient.buscarHabitacion(
                        empresaId, hostalId, dto.getHabitacionId()
                ),
                "Habitación"
        );

        if (habitacion == null
                || !Objects.equals(habitacion.getEmpresaId(), empresaId)
                || !Objects.equals(habitacion.getHostalId(), hostalId)
                || !Objects.equals(
                habitacion.getHabitacionId(), dto.getHabitacionId()
        )
                || habitacion.getTipoHabitacionId() == null) {
            throw error(
                    HttpStatus.BAD_GATEWAY,
                    "El servicio devolvió una habitación inesperada"
            );
        }

        if (!"HABILITADA".equals(habitacion.getEstadoOperativo())) {
            throw error(
                    HttpStatus.CONFLICT, "La habitación no está habilitada"
            );
        }

        TipoHabitacionResumenDto tipo = consultar(
                () -> habitacionClient.buscarTipoHabitacion(
                        empresaId, hostalId, habitacion.getTipoHabitacionId()
                ),
                "Tipo de habitación"
        );

        if (tipo == null
                || !Objects.equals(tipo.getEmpresaId(), empresaId)
                || !Objects.equals(tipo.getHostalId(), hostalId)
                || !Objects.equals(
                tipo.getTipoHabitacionId(),
                habitacion.getTipoHabitacionId()
        )) {
            throw error(
                    HttpStatus.BAD_GATEWAY,
                    "El servicio devolvió un tipo de habitación inesperado"
            );
        }

        if (!Boolean.TRUE.equals(tipo.getEstado())) {
            throw error(
                    HttpStatus.CONFLICT, "El tipo de habitación está inactivo"
            );
        }

        if (tipo.getCapacidad() == null || tipo.getCapacidad() <= 0
                || tipo.getPrecioNoche() == null
                || tipo.getPrecioNoche().signum() < 0) {
            throw error(
                    HttpStatus.BAD_GATEWAY,
                    "El tipo de habitación tiene capacidad o tarifa inválida"
            );
        }

        if (dto.getCantidadPersonas() > tipo.getCapacidad()) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "La cantidad de personas supera la capacidad de "
                            + tipo.getCapacidad()
            );
        }

        long noches = calcularNoches(dto, hostal);

        if (reservaRepository.existeCruce(
                empresaId, hostalId, dto.getHabitacionId(),
                dto.getEntrada(), dto.getSalida()
        )) {
            throw error(
                    HttpStatus.CONFLICT,
                    "La habitación ya tiene una reserva en esas fechas"
            );
        }

        BigDecimal total = tipo.getPrecioNoche()
                .multiply(BigDecimal.valueOf(noches))
                .setScale(2, RoundingMode.HALF_UP);

        if (total.compareTo(new BigDecimal("9999999999.99")) > 0) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "El total supera el importe permitido"
            );
        }

        Reserva reserva = Reserva.builder()
                .empresaId(empresaId)
                .hostalId(hostalId)
                .habitacionId(dto.getHabitacionId())
                .huespedId(dto.getHuespedId())
                .entrada(dto.getEntrada())
                .salida(dto.getSalida())
                .cantidadPersonas(dto.getCantidadPersonas())
                .total(total)
                .moneda(hostal.getMoneda())
                .estado("CONFIRMADA")
                .origen(dto.getOrigen())
                .observaciones(dto.getObservaciones())
                .build();

        try {
            return convertir(reservaRepository.saveAndFlush(reserva));
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se pudo registrar la reserva: existe un conflicto "
                            + "de disponibilidad o de integridad de datos",
                    ex
            );
        }
    }

    @Override
    public List<ReservaResponseDto> listarReservas(
            Long empresaId, Long hostalId
    ) {
        validarHostal(empresaId, hostalId);

        return reservaRepository
                .findByEmpresaIdAndHostalIdOrderByEntradaAsc(empresaId, hostalId)
                .stream()
                .map(this::convertir)
                .toList();
    }

    @Override
    public ReservaResponseDto buscarReserva(
            Long empresaId, Long hostalId, Long reservaId
    ) {
        validarId(reservaId, "reserva");
        validarHostal(empresaId, hostalId);

        Reserva reserva = reservaRepository
                .findByReservaIdAndEmpresaIdAndHostalId(
                        reservaId, empresaId, hostalId
                )
                .orElseThrow(() -> error(
                        HttpStatus.NOT_FOUND,
                        "Reserva no encontrada en este hostal"
                ));

        return convertir(reserva);
    }

    private HostalResumenDto validarHostal(Long empresaId, Long hostalId) {
        validarId(hostalId, "hostal");
        empresaValidator.validarActiva(empresaId);

        HostalResumenDto hostal = consultar(
                () -> hostalClient.buscarHostal(empresaId, hostalId),
                "Hostal"
        );

        if (hostal == null
                || !Objects.equals(hostal.getEmpresaId(), empresaId)
                || !Objects.equals(hostal.getHostalId(), hostalId)) {
            throw error(
                    HttpStatus.BAD_GATEWAY,
                    "El servicio devolvió un hostal inesperado"
            );
        }

        if (!Boolean.TRUE.equals(hostal.getEstado())) {
            throw error(HttpStatus.CONFLICT, "El hostal está inactivo");
        }

        return hostal;
    }

    private void validarDatos(ReservaDto dto) {
        if (dto == null) {
            throw error(
                    HttpStatus.BAD_REQUEST, "Los datos de reserva son obligatorios"
            );
        }

        validarId(dto.getHabitacionId(), "habitación");
        validarId(dto.getHuespedId(), "huésped");

        if (dto.getEntrada() == null || dto.getSalida() == null) {
            throw error(
                    HttpStatus.BAD_REQUEST, "Entrada y salida son obligatorias"
            );
        }

        if (!dto.getSalida().isAfter(dto.getEntrada())) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "La salida debe ser posterior a la entrada"
            );
        }

        if (dto.getCantidadPersonas() == null
                || dto.getCantidadPersonas() <= 0) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "La cantidad de personas debe ser positiva"
            );
        }

        if (!"RECEPCION".equals(dto.getOrigen())
                && !"WEB".equals(dto.getOrigen())) {
            throw error(
                    HttpStatus.BAD_REQUEST, "El origen debe ser RECEPCION o WEB"
            );
        }
    }

    private long calcularNoches(ReservaDto dto, HostalResumenDto hostal) {
        if (hostal.getZonaHoraria() == null
                || hostal.getMoneda() == null
                || !hostal.getMoneda().matches("[A-Z]{3}")) {
            throw error(
                    HttpStatus.BAD_GATEWAY,
                    "El hostal tiene una zona horaria o moneda inválida"
            );
        }

        ZoneId zona;
        try {
            zona = ZoneId.of(hostal.getZonaHoraria());
        } catch (DateTimeException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "La zona horaria del hostal no es válida",
                    ex
            );
        }

        long noches = ChronoUnit.DAYS.between(
                dto.getEntrada().atZoneSameInstant(zona).toLocalDate(),
                dto.getSalida().atZoneSameInstant(zona).toLocalDate()
        );

        if (noches < 1) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "La reserva debe incluir al menos una noche"
            );
        }

        return noches;
    }

    private <T> T consultar(Supplier<T> consulta, String recurso) {
        try {
            return consulta.get();
        } catch (FeignException ex) {
            if (ex.status() == 404) {
                throw error(
                        HttpStatus.NOT_FOUND,
                        recurso + " no encontrado en la empresa y hostal indicados"
                );
            }

            if (ex.status() == 409) {
                throw error(
                        HttpStatus.CONFLICT,
                        recurso + " no disponible: revisa el estado de la empresa y hostal"
                );
            }

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo consultar: " + recurso,
                    ex
            );
        }
    }

    private void validarId(Long id, String recurso) {
        if (id == null || id <= 0) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "El ID de " + recurso + " debe ser positivo"
            );
        }
    }

    private ResponseStatusException error(HttpStatus estado, String mensaje) {
        return new ResponseStatusException(estado, mensaje);
    }

    private ReservaResponseDto convertir(Reserva reserva) {
        return ReservaResponseDto.builder()
                .reservaId(reserva.getReservaId())
                .empresaId(reserva.getEmpresaId())
                .hostalId(reserva.getHostalId())
                .habitacionId(reserva.getHabitacionId())
                .huespedId(reserva.getHuespedId())
                .entrada(reserva.getEntrada())
                .salida(reserva.getSalida())
                .cantidadPersonas(reserva.getCantidadPersonas())
                .total(reserva.getTotal())
                .moneda(reserva.getMoneda())
                .estado(reserva.getEstado())
                .origen(reserva.getOrigen())
                .observaciones(reserva.getObservaciones())
                .creadoEn(reserva.getCreadoEn())
                .build();
    }
}