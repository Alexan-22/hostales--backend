package com.hostales.habitacion.usecase;

import com.hostales.habitacion.bean.dto.*;
import java.util.List;

public interface TipoHabitacionUseCase {

    TipoHabitacionResponseDto registrarTipo(
            Long empresaId, Long hostalId, TipoHabitacionDto dto
    );

    List<TipoHabitacionResponseDto> listarTipos(Long empresaId, Long hostalId);

    TipoHabitacionResponseDto buscarTipo(
            Long empresaId, Long hostalId, Long tipoHabitacionId
    );
}
