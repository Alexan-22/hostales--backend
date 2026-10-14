package com.hostales.habitacion.usecase;

import com.hostales.habitacion.bean.dto.*;
import java.util.List;

public interface HabitacionUseCase {

    HabitacionResponseDto registrarHabitacion(
            Long empresaId, Long hostalId, HabitacionDto dto
    );

    List<HabitacionResponseDto> listarHabitaciones(Long empresaId, Long hostalId);

    HabitacionResponseDto buscarHabitacion(
            Long empresaId, Long hostalId, Long habitacionId
    );
}
