package com.hostales.hostal.model;

import com.hostales.hostal.bean.dto.*;
import com.hostales.hostal.bean.entity.Empresa;
import com.hostales.hostal.bean.entity.Hostal;
import com.hostales.hostal.persistence.EmpresaRepository;
import com.hostales.hostal.persistence.HostalRepository;
import com.hostales.hostal.usecase.HostalUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class HostalModel implements HostalUseCase {

    private final HostalRepository hostalRepository;
    private final EmpresaRepository empresaRepository;

    @Override
    public HostalResponseDto registrarHostal(Long empresaId, HostalDto dto) {
        Empresa empresa = buscarEmpresaActiva(empresaId);
        String nombre = dto.getNombre().trim();

        if (hostalRepository.existsByEmpresa_EmpresaIdAndNombre(empresaId, nombre)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Ya existe un hostal con ese nombre en la empresa"
            );
        }

        try {
            ZoneId.of(dto.getZonaHoraria());
        } catch (DateTimeException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Zona horaria inválida"
            );
        }

        Hostal hostal = Hostal.builder()
                .empresa(empresa)
                .nombre(nombre)
                .direccion(dto.getDireccion().trim())
                .departamento(dto.getDepartamento().trim())
                .provincia(dto.getProvincia().trim())
                .distrito(dto.getDistrito().trim())
                .telefono(dto.getTelefono())
                .correo(dto.getCorreo())
                .horaEntrada(dto.getHoraEntrada())
                .horaSalida(dto.getHoraSalida())
                .zonaHoraria(dto.getZonaHoraria())
                .moneda(dto.getMoneda())
                .build();

        return convertir(hostalRepository.save(hostal));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HostalResponseDto> listarHostales(Long empresaId) {
        buscarEmpresaActiva(empresaId);

        return hostalRepository.findByEmpresa_EmpresaId(empresaId)
                .stream()
                .map(this::convertir)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public HostalResponseDto buscarHostal(Long empresaId, Long hostalId) {
        buscarEmpresaActiva(empresaId);

        Hostal hostal = hostalRepository
                .findByHostalIdAndEmpresa_EmpresaId(hostalId, empresaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Hostal no encontrado en esta empresa"
                ));

        return convertir(hostal);
    }

    private Empresa buscarEmpresaActiva(Long empresaId) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Empresa no encontrada"
                ));

        if (!Boolean.TRUE.equals(empresa.getEstado())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "La empresa está inactiva"
            );
        }

        return empresa;
    }

    private HostalResponseDto convertir(Hostal hostal) {
        return HostalResponseDto.builder()
                .hostalId(hostal.getHostalId())
                .empresaId(hostal.getEmpresa().getEmpresaId())
                .nombre(hostal.getNombre())
                .direccion(hostal.getDireccion())
                .departamento(hostal.getDepartamento())
                .provincia(hostal.getProvincia())
                .distrito(hostal.getDistrito())
                .telefono(hostal.getTelefono())
                .correo(hostal.getCorreo())
                .horaEntrada(hostal.getHoraEntrada())
                .horaSalida(hostal.getHoraSalida())
                .zonaHoraria(hostal.getZonaHoraria())
                .moneda(hostal.getMoneda())
                .estado(hostal.getEstado())
                .creadoEn(hostal.getCreadoEn())
                .build();
    }
}