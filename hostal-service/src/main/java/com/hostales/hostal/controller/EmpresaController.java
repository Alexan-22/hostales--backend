package com.hostales.hostal.controller;

import com.hostales.hostal.bean.dto.EmpresaDto;
import com.hostales.hostal.bean.dto.EmpresaResponseDto;
import com.hostales.hostal.usecase.EmpresaUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/empresas")
public class EmpresaController {

    private final EmpresaUseCase empresaUseCase;

    @PostMapping
    public ResponseEntity<EmpresaResponseDto> registrar(
            @Valid @RequestBody EmpresaDto dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(empresaUseCase.registrarEmpresa(dto));
    }

    @GetMapping("/{empresaId}")
    public EmpresaResponseDto buscar(@PathVariable Long empresaId) {
        return empresaUseCase.buscarEmpresa(empresaId);
    }
}