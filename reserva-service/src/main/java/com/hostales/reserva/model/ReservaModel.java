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
import java.time.OffsetDateTime;
import com.hostales.reserva.bean.dto.DisponibilidadResponseDto;
import com.hostales.reserva.bean.dto.HabitacionDisponibleDto;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
                .entradaReal(reserva.getEntradaReal())
                .salidaReal(reserva.getSalidaReal())
                .build();
    }

    @Override
    public ReservaResponseDto cancelarReserva(
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

        if ("CANCELADA".equals(reserva.getEstado())) {
            return convertir(reserva);
        }

        if (!"CONFIRMADA".equals(reserva.getEstado())) {
            throw error(
                    HttpStatus.CONFLICT,
                    "Solo se pueden cancelar reservas confirmadas"
            );
        }

        int actualizadas = reservaRepository.cancelarConfirmada(
                reservaId, empresaId, hostalId
        );

        Reserva resultado = reservaRepository
                .findByReservaIdAndEmpresaIdAndHostalId(
                        reservaId, empresaId, hostalId
                )
                .orElseThrow(() -> error(
                        HttpStatus.NOT_FOUND,
                        "Reserva no encontrada en este hostal"
                ));

        if (actualizadas == 0 && !"CANCELADA".equals(resultado.getEstado())) {
            throw error(
                    HttpStatus.CONFLICT,
                    "La reserva cambió de estado y no se pudo cancelar"
            );
        }

        return convertir(resultado);
    }

    @Override
    public ReservaResponseDto registrarCheckIn(
            Long empresaId, Long hostalId, Long reservaId
    ) {
        Reserva reserva = obtenerParaOperacion(
                empresaId, hostalId, reservaId
        );

        // Repetir la misma operación no modifica nuevamente la reserva.
        if ("EN_CURSO".equals(reserva.getEstado())) {
            return convertir(reserva);
        }

        if (!"CONFIRMADA".equals(reserva.getEstado())) {
            throw error(
                    HttpStatus.CONFLICT,
                    "Solo se puede registrar el check-in de una reserva confirmada"
            );
        }

        OffsetDateTime ahora = OffsetDateTime.now();

        if (ahora.isBefore(reserva.getEntrada())) {
            throw error(
                    HttpStatus.CONFLICT,
                    "Todavía no llegó la fecha y hora de entrada"
            );
        }

        if (!ahora.isBefore(reserva.getSalida())) {
            throw error(
                    HttpStatus.CONFLICT,
                    "El período de la reserva ya terminó"
            );
        }

        return aplicarCambioEstado(reserva, "CONFIRMADA", "EN_CURSO");
    }

    @Override
    public ReservaResponseDto registrarCheckOut(
            Long empresaId, Long hostalId, Long reservaId
    ) {
        Reserva reserva = obtenerParaOperacion(
                empresaId, hostalId, reservaId
        );

        if ("FINALIZADA".equals(reserva.getEstado())) {
            return convertir(reserva);
        }

        if (!"EN_CURSO".equals(reserva.getEstado())) {
            throw error(
                    HttpStatus.CONFLICT,
                    "Solo se puede registrar el check-out de una reserva en curso"
            );
        }

        return aplicarCambioEstado(reserva, "EN_CURSO", "FINALIZADA");
    }

    private Reserva obtenerParaOperacion(
            Long empresaId, Long hostalId, Long reservaId
    ) {
        validarId(reservaId, "reserva");
        validarHostal(empresaId, hostalId);

        return reservaRepository
                .findByReservaIdAndEmpresaIdAndHostalId(
                        reservaId, empresaId, hostalId
                )
                .orElseThrow(() -> error(
                        HttpStatus.NOT_FOUND,
                        "Reserva no encontrada en este hostal"
                ));
    }

    private ReservaResponseDto aplicarCambioEstado(
            Reserva reserva, String estadoActual, String estadoNuevo
    ) {
        int actualizadas = reservaRepository.cambiarEstado(
                reserva.getReservaId(),
                reserva.getEmpresaId(),
                reserva.getHostalId(),
                estadoActual,
                estadoNuevo,
                OffsetDateTime.now()
        );

        Reserva resultado = reservaRepository
                .findByReservaIdAndEmpresaIdAndHostalId(
                        reserva.getReservaId(),
                        reserva.getEmpresaId(),
                        reserva.getHostalId()
                )
                .orElseThrow(() -> error(
                        HttpStatus.NOT_FOUND,
                        "Reserva no encontrada en este hostal"
                ));

        if (actualizadas == 0
                && !estadoNuevo.equals(resultado.getEstado())) {
            throw error(
                    HttpStatus.CONFLICT,
                    "La reserva cambió de estado. Consulta sus datos nuevamente"
            );
        }

        return convertir(resultado);
    }

    @Override
    public DisponibilidadResponseDto consultarDisponibilidad(
            Long empresaId,
            Long hostalId,
            Long habitacionId,
            OffsetDateTime entrada,
            OffsetDateTime salida
    ) {
        validarId(habitacionId, "habitación");

        if (entrada == null || salida == null || !salida.isAfter(entrada)) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "La entrada y salida son obligatorias y la salida debe ser posterior"
            );
        }

        HostalResumenDto hostal = validarHostal(empresaId, hostalId);

        // Aplica la misma regla de al menos una noche del registro.
        calcularNoches(
                ReservaDto.builder()
                        .entrada(entrada)
                        .salida(salida)
                        .build(),
                hostal
        );

        HabitacionResumenDto habitacion = consultar(
                () -> habitacionClient.buscarHabitacion(
                        empresaId, hostalId, habitacionId
                ),
                "Habitación"
        );

        if (habitacion == null
                || !Objects.equals(habitacion.getEmpresaId(), empresaId)
                || !Objects.equals(habitacion.getHostalId(), hostalId)
                || !Objects.equals(habitacion.getHabitacionId(), habitacionId)
                || habitacion.getTipoHabitacionId() == null) {
            throw error(
                    HttpStatus.BAD_GATEWAY,
                    "El servicio devolvió una habitación inesperada"
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

        boolean disponible = false;
        String motivo;

        if (!"HABILITADA".equals(habitacion.getEstadoOperativo())) {
            motivo = "La habitación no está habilitada";
        } else if (!Boolean.TRUE.equals(tipo.getEstado())) {
            motivo = "El tipo de habitación está inactivo";
        } else if (reservaRepository.existeCruce(
                empresaId, hostalId, habitacionId, entrada, salida
        )) {
            motivo = "La habitación tiene una reserva en ese intervalo";
        } else {
            disponible = true;
            motivo = "Habitación disponible en el intervalo consultado";
        }

        return DisponibilidadResponseDto.builder()
                .empresaId(empresaId)
                .hostalId(hostalId)
                .habitacionId(habitacionId)
                .entrada(entrada)
                .salida(salida)
                .disponible(disponible)
                .motivo(motivo)
                .build();
    }
    @Override
    public List<HabitacionDisponibleDto> listarHabitacionesDisponibles(
            Long empresaId,
            Long hostalId,
            OffsetDateTime entrada,
            OffsetDateTime salida,
            Integer cantidadPersonas
    ) {
        if (entrada == null || salida == null || !salida.isAfter(entrada)) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "La salida debe ser posterior a la entrada"
            );
        }

        if (cantidadPersonas == null || cantidadPersonas <= 0) {
            throw error(
                    HttpStatus.BAD_REQUEST,
                    "La cantidad de personas debe ser positiva"
            );
        }

        HostalResumenDto hostal = validarHostal(empresaId, hostalId);

        long noches = calcularNoches(
                ReservaDto.builder()
                        .entrada(entrada)
                        .salida(salida)
                        .build(),
                hostal
        );

        List<HabitacionResumenDto> habitaciones = consultar(
                () -> habitacionClient.listarHabitaciones(empresaId, hostalId),
                "Habitaciones"
        );

        List<TipoHabitacionResumenDto> tipos = consultar(
                () -> habitacionClient.listarTiposHabitacion(empresaId, hostalId),
                "Tipos de habitación"
        );

        if (habitaciones == null || tipos == null) {
            throw error(
                    HttpStatus.BAD_GATEWAY,
                    "El servicio de habitaciones devolvió una respuesta inválida"
            );
        }

        Map<Long, TipoHabitacionResumenDto> tiposPorId = new HashMap<>();

        for (TipoHabitacionResumenDto tipo : tipos) {
            if (tipo == null
                    || tipo.getTipoHabitacionId() == null
                    || !Objects.equals(tipo.getEmpresaId(), empresaId)
                    || !Objects.equals(tipo.getHostalId(), hostalId)) {
                throw error(
                        HttpStatus.BAD_GATEWAY,
                        "El servicio devolvió un tipo de habitación inesperado"
                );
            }

            if (tiposPorId.putIfAbsent(
                    tipo.getTipoHabitacionId(), tipo
            ) != null) {
                throw error(
                        HttpStatus.BAD_GATEWAY,
                        "El servicio devolvió tipos de habitación duplicados"
                );
            }
        }

        Set<Long> ocupadas = new HashSet<>(
                reservaRepository.buscarHabitacionesConCruce(
                        empresaId, hostalId, entrada, salida
                )
        );

        List<HabitacionDisponibleDto> disponibles = new ArrayList<>();

        for (HabitacionResumenDto habitacion : habitaciones) {
            if (habitacion == null
                    || habitacion.getHabitacionId() == null
                    || habitacion.getTipoHabitacionId() == null
                    || habitacion.getNumero() == null
                    || !Objects.equals(habitacion.getEmpresaId(), empresaId)
                    || !Objects.equals(habitacion.getHostalId(), hostalId)) {
                throw error(
                        HttpStatus.BAD_GATEWAY,
                        "El servicio devolvió una habitación inesperada"
                );
            }

            if (!"HABILITADA".equals(habitacion.getEstadoOperativo())
                    || ocupadas.contains(habitacion.getHabitacionId())) {
                continue;
            }

            TipoHabitacionResumenDto tipo = tiposPorId.get(
                    habitacion.getTipoHabitacionId()
            );

            if (tipo == null) {
                throw error(
                        HttpStatus.BAD_GATEWAY,
                        "No se recibió el tipo de una habitación"
                );
            }

            if (!Boolean.TRUE.equals(tipo.getEstado())) {
                continue;
            }

            if (tipo.getCapacidad() == null || tipo.getCapacidad() <= 0
                    || tipo.getPrecioNoche() == null
                    || tipo.getPrecioNoche().signum() < 0) {
                throw error(
                        HttpStatus.BAD_GATEWAY,
                        "El tipo de habitación tiene capacidad o tarifa inválida"
                );
            }

            if (tipo.getCapacidad() < cantidadPersonas) {
                continue;
            }

            BigDecimal total = tipo.getPrecioNoche()
                    .multiply(BigDecimal.valueOf(noches))
                    .setScale(2, RoundingMode.HALF_UP);

            disponibles.add(
                    HabitacionDisponibleDto.builder()
                            .habitacionId(habitacion.getHabitacionId())
                            .tipoHabitacionId(tipo.getTipoHabitacionId())
                            .numero(habitacion.getNumero())
                            .tipo(tipo.getNombre())
                            .capacidad(tipo.getCapacidad())
                            .precioNoche(tipo.getPrecioNoche())
                            .noches(noches)
                            .totalEstimado(total)
                            .moneda(hostal.getMoneda())
                            .build()
            );
        }

        disponibles.sort(
                Comparator.comparing(HabitacionDisponibleDto::getPrecioNoche)
                        .thenComparing(HabitacionDisponibleDto::getNumero)
        );

        return disponibles;
    }


}