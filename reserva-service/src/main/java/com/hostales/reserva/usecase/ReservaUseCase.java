package com.hostales.reserva.usecase;

import com.hostales.reserva.bean.dto.ReservaDto;
import com.hostales.reserva.bean.dto.ReservaResponseDto;
import java.util.List;

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
}