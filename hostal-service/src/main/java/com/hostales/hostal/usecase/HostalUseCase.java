package com.hostales.hostal.usecase;

import com.hostales.hostal.bean.dto.HostalDto;
import com.hostales.hostal.bean.dto.HostalResponseDto;
import java.util.List;

public interface HostalUseCase {

    HostalResponseDto registrarHostal(Long empresaId, HostalDto dto);

    List<HostalResponseDto> listarHostales(Long empresaId);

    HostalResponseDto buscarHostal(Long empresaId, Long hostalId);
}
