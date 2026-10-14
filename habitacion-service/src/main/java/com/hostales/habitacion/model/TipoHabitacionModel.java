package com.hostales.habitacion.model;

import com.hostales.habitacion.bean.dto.*;
import com.hostales.habitacion.bean.entity.TipoHabitacion;
import com.hostales.habitacion.persistence.TipoHabitacionRepository;
import com.hostales.habitacion.usecase.TipoHabitacionUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TipoHabitacionModel implements TipoHabitacionUseCase {

    private final TipoHabitacionRepository tipoHabitacionRepository;
    private final HostalValidator hostalValidator;

    @Override
    public TipoHabitacionResponseDto registrarTipo(
            Long empresaId, Long hostalId, TipoHabitacionDto dto
    ) {
        hostalValidator.validarActivo(empresaId, hostalId);
        String nombre = dto.getNombre().trim();

        if (tipoHabitacionRepository.existsByEmpresaIdAndHostalIdAndNombre(
                empresaId, hostalId, nombre
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Ya existe un tipo con ese nombre en el hostal"
            );
        }

        TipoHabitacion tipo = TipoHabitacion.builder()
                .empresaId(empresaId)
                .hostalId(hostalId)
                .nombre(nombre)
                .descripcion(dto.getDescripcion())
                .capacidad(dto.getCapacidad())
                .precioNoche(dto.getPrecioNoche())
                .build();

        try {
            return convertir(tipoHabitacionRepository.saveAndFlush(tipo));
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se pudo guardar el tipo: los datos entran en conflicto con las restricciones",
                    ex
            );
        }
    }

    @Override
    public List<TipoHabitacionResponseDto> listarTipos(Long empresaId, Long hostalId) {
        hostalValidator.validarActivo(empresaId, hostalId);

        return tipoHabitacionRepository
                .findByEmpresaIdAndHostalIdOrderByNombreAsc(empresaId, hostalId)
                .stream()
                .map(this::convertir)
                .toList();
    }

    @Override
    public TipoHabitacionResponseDto buscarTipo(
            Long empresaId, Long hostalId, Long tipoHabitacionId
    ) {
        hostalValidator.validarActivo(empresaId, hostalId);

        TipoHabitacion tipo = tipoHabitacionRepository
                .findByTipoHabitacionIdAndEmpresaIdAndHostalId(
                        tipoHabitacionId, empresaId, hostalId
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Tipo de habitación no encontrado en este hostal"
                ));

        return convertir(tipo);
    }

    private TipoHabitacionResponseDto convertir(TipoHabitacion tipo) {
        return TipoHabitacionResponseDto.builder()
                .tipoHabitacionId(tipo.getTipoHabitacionId())
                .empresaId(tipo.getEmpresaId())
                .hostalId(tipo.getHostalId())
                .nombre(tipo.getNombre())
                .descripcion(tipo.getDescripcion())
                .capacidad(tipo.getCapacidad())
                .precioNoche(tipo.getPrecioNoche())
                .estado(tipo.getEstado())
                .creadoEn(tipo.getCreadoEn())
                .build();
    }
}