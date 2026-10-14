package com.hostales.reserva.usecase;

import com.hostales.reserva.bean.dto.HuespedDto;
import com.hostales.reserva.bean.dto.HuespedResponseDto;
import java.util.List;

public interface HuespedUseCase {

    HuespedResponseDto registrarHuesped(Long empresaId, HuespedDto dto);

    List<HuespedResponseDto> listarHuespedes(Long empresaId);

    HuespedResponseDto buscarHuesped(Long empresaId, Long huespedId);
}
