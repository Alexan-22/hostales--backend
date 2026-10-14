package com.hostales.habitacion.controller;

import com.hostales.habitacion.bean.dto.*;
import com.hostales.habitacion.usecase.TipoHabitacionUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/empresas/{empresaId}/hostales/{hostalId}/tipos-habitacion")
public class TipoHabitacionController {

    private final TipoHabitacionUseCase tipoHabitacionUseCase;

    @PostMapping
    public ResponseEntity<TipoHabitacionResponseDto> registrar(
            @PathVariable Long empresaId,
            @PathVariable Long hostalId,
            @Valid @RequestBody TipoHabitacionDto dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tipoHabitacionUseCase.registrarTipo(empresaId, hostalId, dto));
    }

    @GetMapping
    public List<TipoHabitacionResponseDto> listar(
            @PathVariable Long empresaId,
            @PathVariable Long hostalId
    ) {
        return tipoHabitacionUseCase.listarTipos(empresaId, hostalId);
    }

    @GetMapping("/{tipoHabitacionId}")
    public TipoHabitacionResponseDto buscar(
            @PathVariable Long empresaId,
            @PathVariable Long hostalId,
            @PathVariable Long tipoHabitacionId
    ) {
        return tipoHabitacionUseCase.buscarTipo(
                empresaId, hostalId, tipoHabitacionId
        );
    }
}
