package com.hostales.reserva.model;

import com.hostales.reserva.bean.dto.HuespedDto;
import com.hostales.reserva.bean.dto.HuespedResponseDto;
import com.hostales.reserva.bean.entity.Huesped;
import com.hostales.reserva.persistence.HuespedRepository;
import com.hostales.reserva.usecase.HuespedUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HuespedModel implements HuespedUseCase {

    private final HuespedRepository huespedRepository;
    private final EmpresaValidator empresaValidator;

    @Override
    public HuespedResponseDto registrarHuesped(Long empresaId, HuespedDto dto) {
        empresaValidator.validarActiva(empresaId);
        String documento = dto.getNumeroDocumento().trim();

        if ("DNI".equals(dto.getTipoDocumento()) && !documento.matches("[0-9]{8}")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "El DNI debe tener exactamente 8 dígitos"
            );
        }

        if (huespedRepository.existsByEmpresaIdAndTipoDocumentoAndNumeroDocumento(
                empresaId, dto.getTipoDocumento(), documento
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "El documento ya está registrado en esta empresa"
            );
        }

        Huesped huesped = Huesped.builder()
                .empresaId(empresaId)
                .tipoDocumento(dto.getTipoDocumento())
                .numeroDocumento(documento)
                .nombre(dto.getNombre().trim())
                .apellido(dto.getApellido().trim())
                .correo(dto.getCorreo())
                .telefono(dto.getTelefono())
                .build();

        try {
            return convertir(huespedRepository.saveAndFlush(huesped));
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se pudo guardar el huésped por un conflicto con las restricciones",
                    ex
            );
        }
    }

    @Override
    public List<HuespedResponseDto> listarHuespedes(Long empresaId) {
        empresaValidator.validarActiva(empresaId);

        return huespedRepository.findByEmpresaIdOrderByApellidoAscNombreAsc(empresaId)
                .stream()
                .map(this::convertir)
                .toList();
    }

    @Override
    public HuespedResponseDto buscarHuesped(Long empresaId, Long huespedId) {
        empresaValidator.validarActiva(empresaId);

        Huesped huesped = huespedRepository
                .findByHuespedIdAndEmpresaId(huespedId, empresaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Huésped no encontrado en esta empresa"
                ));

        return convertir(huesped);
    }

    private HuespedResponseDto convertir(Huesped huesped) {
        return HuespedResponseDto.builder()
                .huespedId(huesped.getHuespedId())
                .empresaId(huesped.getEmpresaId())
                .tipoDocumento(huesped.getTipoDocumento())
                .numeroDocumento(huesped.getNumeroDocumento())
                .nombre(huesped.getNombre())
                .apellido(huesped.getApellido())
                .correo(huesped.getCorreo())
                .telefono(huesped.getTelefono())
                .estado(huesped.getEstado())
                .creadoEn(huesped.getCreadoEn())
                .build();
    }
}
