package com.hostales.habitacion.model;

import com.hostales.habitacion.bean.dto.*;
import com.hostales.habitacion.bean.entity.Habitacion;
import com.hostales.habitacion.bean.entity.TipoHabitacion;
import com.hostales.habitacion.persistence.HabitacionRepository;
import com.hostales.habitacion.persistence.TipoHabitacionRepository;
import com.hostales.habitacion.usecase.HabitacionUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HabitacionModel implements HabitacionUseCase {

    private final HabitacionRepository habitacionRepository;
    private final TipoHabitacionRepository tipoHabitacionRepository;
    private final HostalValidator hostalValidator;

    @Override
    public HabitacionResponseDto registrarHabitacion(
            Long empresaId, Long hostalId, HabitacionDto dto
    ) {
        hostalValidator.validarActivo(empresaId, hostalId);

        TipoHabitacion tipo = tipoHabitacionRepository
                .findByTipoHabitacionIdAndEmpresaIdAndHostalId(
                        dto.getTipoHabitacionId(), empresaId, hostalId
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Tipo de habitación no encontrado en este hostal"
                ));

        if (!Boolean.TRUE.equals(tipo.getEstado())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "El tipo de habitación está inactivo"
            );
        }

        String numero = dto.getNumero().trim();

        if (habitacionRepository.existsByEmpresaIdAndHostalIdAndNumero(
                empresaId, hostalId, numero
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Ya existe una habitación con ese número en el hostal"
            );
        }

        Habitacion habitacion = Habitacion.builder()
                .empresaId(empresaId)
                .hostalId(hostalId)
                .tipoHabitacionId(tipo.getTipoHabitacionId())
                .numero(numero)
                .piso(dto.getPiso())
                .estadoOperativo(dto.getEstadoOperativo())
                .build();

        try {
            return convertir(habitacionRepository.saveAndFlush(habitacion));
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se pudo guardar la habitación: los datos entran en conflicto con las restricciones",
                    ex
            );
        }
    }

    @Override
    public List<HabitacionResponseDto> listarHabitaciones(Long empresaId, Long hostalId) {
        hostalValidator.validarActivo(empresaId, hostalId);

        return habitacionRepository
                .findByEmpresaIdAndHostalIdOrderByNumeroAsc(empresaId, hostalId)
                .stream()
                .map(this::convertir)
                .toList();
    }

    @Override
    public HabitacionResponseDto buscarHabitacion(
            Long empresaId, Long hostalId, Long habitacionId
    ) {
        hostalValidator.validarActivo(empresaId, hostalId);

        Habitacion habitacion = habitacionRepository
                .findByHabitacionIdAndEmpresaIdAndHostalId(
                        habitacionId, empresaId, hostalId
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Habitación no encontrada en este hostal"
                ));

        return convertir(habitacion);
    }

    private HabitacionResponseDto convertir(Habitacion habitacion) {
        return HabitacionResponseDto.builder()
                .habitacionId(habitacion.getHabitacionId())
                .empresaId(habitacion.getEmpresaId())
                .hostalId(habitacion.getHostalId())
                .tipoHabitacionId(habitacion.getTipoHabitacionId())
                .numero(habitacion.getNumero())
                .piso(habitacion.getPiso())
                .estadoOperativo(habitacion.getEstadoOperativo())
                .creadoEn(habitacion.getCreadoEn())
                .build();
    }
}
