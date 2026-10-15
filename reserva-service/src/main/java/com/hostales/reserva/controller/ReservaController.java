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
import org.springframework.web.bind.annotation.PatchMapping;
import com.hostales.reserva.bean.dto.DisponibilidadResponseDto;
import java.time.OffsetDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;
import com.hostales.reserva.bean.dto.HabitacionDisponibleDto;

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

    @PatchMapping("/{reservaId}/cancelar")
    public ReservaResponseDto cancelar(
            @PathVariable("empresaId") Long empresaId,
            @PathVariable("hostalId") Long hostalId,
            @PathVariable("reservaId") Long reservaId
    ) {
        return reservaUseCase.cancelarReserva(
                empresaId, hostalId, reservaId
        );
    }
    @PatchMapping("/{reservaId}/check-in")
    public ReservaResponseDto checkIn(
            @PathVariable("empresaId") Long empresaId,
            @PathVariable("hostalId") Long hostalId,
            @PathVariable("reservaId") Long reservaId
    ) {
        return reservaUseCase.registrarCheckIn(
                empresaId, hostalId, reservaId
        );
    }

    @PatchMapping("/{reservaId}/check-out")
    public ReservaResponseDto checkOut(
            @PathVariable("empresaId") Long empresaId,
            @PathVariable("hostalId") Long hostalId,
            @PathVariable("reservaId") Long reservaId
    ) {
        return reservaUseCase.registrarCheckOut(
                empresaId, hostalId, reservaId
        );
    }
    @GetMapping("/disponibilidad")
    public DisponibilidadResponseDto disponibilidad(
            @PathVariable("empresaId") Long empresaId,
            @PathVariable("hostalId") Long hostalId,
            @RequestParam("habitacionId") Long habitacionId,
            @RequestParam("entrada")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime entrada,
            @RequestParam("salida")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime salida
    ) {
        return reservaUseCase.consultarDisponibilidad(
                empresaId, hostalId, habitacionId, entrada, salida
        );
    }
    @GetMapping("/habitaciones-disponibles")
    public List<HabitacionDisponibleDto> habitacionesDisponibles(
            @PathVariable("empresaId") Long empresaId,
            @PathVariable("hostalId") Long hostalId,
            @RequestParam("entrada")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime entrada,
            @RequestParam("salida")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime salida,
            @RequestParam(value = "cantidadPersonas", defaultValue = "1")
            Integer cantidadPersonas
    ) {
        return reservaUseCase.listarHabitacionesDisponibles(
                empresaId, hostalId, entrada, salida, cantidadPersonas
        );
    }

}
