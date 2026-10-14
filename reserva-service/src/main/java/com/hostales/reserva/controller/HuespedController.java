package com.hostales.reserva.controller;

import com.hostales.reserva.bean.dto.HuespedDto;
import com.hostales.reserva.bean.dto.HuespedResponseDto;
import com.hostales.reserva.usecase.HuespedUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/empresas/{empresaId}/huespedes")
public class HuespedController {

    private final HuespedUseCase huespedUseCase;

    @PostMapping
    public ResponseEntity<HuespedResponseDto> registrar(
            @PathVariable Long empresaId,
            @Valid @RequestBody HuespedDto dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(huespedUseCase.registrarHuesped(empresaId, dto));
    }

    @GetMapping
    public List<HuespedResponseDto> listar(@PathVariable Long empresaId) {
        return huespedUseCase.listarHuespedes(empresaId);
    }

    @GetMapping("/{huespedId}")
    public HuespedResponseDto buscar(
            @PathVariable Long empresaId,
            @PathVariable Long huespedId
    ) {
        return huespedUseCase.buscarHuesped(empresaId, huespedId);
    }
}
