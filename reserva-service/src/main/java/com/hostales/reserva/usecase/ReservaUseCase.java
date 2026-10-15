package com.hostales.reserva.usecase;

import com.hostales.reserva.bean.dto.DisponibilidadResponseDto;
import com.hostales.reserva.bean.dto.ReservaDto;
import com.hostales.reserva.bean.dto.ReservaResponseDto;

import java.time.OffsetDateTime;
import java.util.List;
import com.hostales.reserva.bean.dto.HabitacionDisponibleDto;

public interface ReservaUseCase {

    ReservaResponseDto registrarReserva(
            Long empresaId, Long hostalId, ReservaDto dto
    );

    List<ReservaResponseDto> listarReservas(
            Long empresaId, Long hostalId
    );

    ReservaResponseDto buscarReserva(
            Long empresaId, Long hostalId, Long reservaId
    );

    ReservaResponseDto cancelarReserva(
            Long empresaId, Long hostalId, Long reservaId
    );

    ReservaResponseDto registrarCheckIn(
            Long empresaId, Long hostalId, Long reservaId
    );

    ReservaResponseDto registrarCheckOut(
            Long empresaId, Long hostalId, Long reservaId
    );

    DisponibilidadResponseDto consultarDisponibilidad(
            Long empresaId,
            Long hostalId,
            Long habitacionId,
            OffsetDateTime entrada,
            OffsetDateTime salida
    );
    List<HabitacionDisponibleDto> listarHabitacionesDisponibles(
            Long empresaId,
            Long hostalId,
            OffsetDateTime entrada,
            OffsetDateTime salida,
            Integer cantidadPersonas
    );


}