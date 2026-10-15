package com.hostales.pago.controller;

import com.hostales.pago.bean.dto.*;
import com.hostales.pago.usecase.PagoUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(
        "/api/empresas/{empresaId}"
                + "/hostales/{hostalId}"
                + "/reservas/{reservaId}/pagos"
)
public class PagoController {

    private final PagoUseCase pagoUseCase;

    @PostMapping
    public PagoResponseDto registrar(
            @PathVariable Long empresaId,
            @PathVariable Long hostalId,
            @PathVariable Long reservaId,
            @Valid @RequestBody PagoDto dto
    ) {
        return pagoUseCase.registrarPago(
                empresaId,
                hostalId,
                reservaId,
                dto
        );
    }

    @GetMapping
    public List<PagoResponseDto> listar(
            @PathVariable Long empresaId,
            @PathVariable Long hostalId,
            @PathVariable Long reservaId
    ) {
        return pagoUseCase.listarPagos(
                empresaId,
                hostalId,
                reservaId
        );
    }

    @GetMapping("/saldo")
    public SaldoReservaDto saldo(
            @PathVariable Long empresaId,
            @PathVariable Long hostalId,
            @PathVariable Long reservaId
    ) {
        return pagoUseCase.consultarSaldo(
                empresaId,
                hostalId,
                reservaId
        );
    }

    @PatchMapping("/{pagoId}/anular")
    public PagoResponseDto anular(
            @PathVariable Long empresaId,
            @PathVariable Long hostalId,
            @PathVariable Long reservaId,
            @PathVariable Long pagoId,
            @Valid @RequestBody AnularPagoDto dto
    ) {
        return pagoUseCase.anularPago(
                empresaId,
                hostalId,
                reservaId,
                pagoId,
                dto
        );
    }
}
