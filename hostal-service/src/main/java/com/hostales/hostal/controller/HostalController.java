package com.hostales.hostal.controller;

import com.hostales.hostal.bean.dto.HostalDto;
import com.hostales.hostal.bean.dto.HostalResponseDto;
import com.hostales.hostal.usecase.HostalUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/empresas/{empresaId}/hostales")
public class HostalController {

    private final HostalUseCase hostalUseCase;

    @PostMapping
    public ResponseEntity<HostalResponseDto> registrar(
            @PathVariable Long empresaId,
            @Valid @RequestBody HostalDto dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(hostalUseCase.registrarHostal(empresaId, dto));
    }

    @GetMapping
    public List<HostalResponseDto> listar(@PathVariable Long empresaId) {
        return hostalUseCase.listarHostales(empresaId);
    }

    @GetMapping("/{hostalId}")
    public HostalResponseDto buscar(
            @PathVariable Long empresaId,
            @PathVariable Long hostalId
    ) {
        return hostalUseCase.buscarHostal(empresaId, hostalId);
    }
}
