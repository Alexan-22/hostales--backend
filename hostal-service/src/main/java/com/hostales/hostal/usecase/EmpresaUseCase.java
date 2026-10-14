package com.hostales.hostal.usecase;

import com.hostales.hostal.bean.dto.EmpresaDto;
import com.hostales.hostal.bean.dto.EmpresaResponseDto;

public interface EmpresaUseCase {

    EmpresaResponseDto registrarEmpresa(EmpresaDto dto);

    EmpresaResponseDto buscarEmpresa(Long empresaId);
}