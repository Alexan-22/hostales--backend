package com.hostales.habitacion.controller;

import com.hostales.habitacion.bean.dto.*;
import com.hostales.habitacion.usecase.HabitacionUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/empresas/{empresaId}/hostales/{hostalId}/habitaciones")
public class HabitacionController {

    private final HabitacionUseCase habitacionUseCase;

    @PostMapping
    public ResponseEntity<HabitacionResponseDto> registrar(
            @PathVariable Long empresaId,
            @PathVariable Long hostalId,
            @Valid @RequestBody HabitacionDto dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(habitacionUseCase.registrarHabitacion(empresaId, hostalId, dto));
    }

    @GetMapping
    public List<HabitacionResponseDto> listar(
            @PathVariable Long empresaId,
            @PathVariable Long hostalId
    ) {
        return habitacionUseCase.listarHabitaciones(empresaId, hostalId);
    }

    @GetMapping("/{habitacionId}")
    public HabitacionResponseDto buscar(
            @PathVariable Long empresaId,
            @PathVariable Long hostalId,
            @PathVariable Long habitacionId
    ) {
        return habitacionUseCase.buscarHabitacion(
                empresaId, hostalId, habitacionId
        );
    }
}