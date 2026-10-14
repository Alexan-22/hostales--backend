package com.hostales.reserva.controller;

import com.hostales.reserva.bean.dto.ReservaDto;
import com.hostales.reserva.bean.dto.ReservaResponseDto;
import com.hostales.reserva.usecase.ReservaUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/empresas/{empresaId}/hostales/{hostalId}/reservas")
public class ReservaController {

    private final ReservaUseCase reservaUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservaResponseDto registrar(
            @PathVariable("empresaId") Long empresaId,
            @PathVariable("hostalId") Long hostalId,
            @Valid @RequestBody ReservaDto dto
    ) {
        return reservaUseCase.registrarReserva(empresaId, hostalId, dto);
    }

    @GetMapping
    public List<ReservaResponseDto> listar(
            @PathVariable("empresaId") Long empresaId,
            @PathVariable("hostalId") Long hostalId
    ) {
        return reservaUseCase.listarReservas(empresaId, hostalId);
    }

    @GetMapping("/{reservaId}")
    public ReservaResponseDto buscar(
            @PathVariable("empresaId") Long empresaId,
            @PathVariable("hostalId") Long hostalId,
            @PathVariable("reservaId") Long reservaId
    ) {
        return reservaUseCase.buscarReserva(empresaId, hostalId, reservaId);
    }
}
