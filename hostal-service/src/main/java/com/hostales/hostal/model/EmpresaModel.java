package com.hostales.hostal.model;

import com.hostales.hostal.bean.dto.*;
import com.hostales.hostal.bean.entity.Empresa;
import com.hostales.hostal.persistence.EmpresaRepository;
import com.hostales.hostal.usecase.EmpresaUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class EmpresaModel implements EmpresaUseCase {

    private final EmpresaRepository empresaRepository;

    @Override
    public EmpresaResponseDto registrarEmpresa(EmpresaDto dto) {
        if (dto.getRuc() != null && empresaRepository.existsByRuc(dto.getRuc())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "El RUC ya está registrado"
            );
        }

        Empresa empresa = Empresa.builder()
                .nombre(dto.getNombre().trim())
                .ruc(dto.getRuc())
                .correo(dto.getCorreo())
                .telefono(dto.getTelefono())
                .build();

        return convertir(empresaRepository.save(empresa));
    }

    @Override
    @Transactional(readOnly = true)
    public EmpresaResponseDto buscarEmpresa(Long empresaId) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Empresa no encontrada"
                ));

        return convertir(empresa);
    }

    private EmpresaResponseDto convertir(Empresa empresa) {
        return EmpresaResponseDto.builder()
                .empresaId(empresa.getEmpresaId())
                .nombre(empresa.getNombre())
                .ruc(empresa.getRuc())
                .correo(empresa.getCorreo())
                .telefono(empresa.getTelefono())
                .estado(empresa.getEstado())
                .creadoEn(empresa.getCreadoEn())
                .build();
    }
}
